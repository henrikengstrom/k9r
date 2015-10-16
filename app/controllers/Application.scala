package controllers

import generators.{MvnGenerator, Generator}
import models.{BuildTool, ProjectDescription}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import generators.GradleGenerator

class Application extends Controller {

  val nameToProjectType = Map(
    "akka" -> models.Akka,
    "play" -> models.Play,
    "scala" -> models.SimpleScala,
    "spark" -> models.Spark
  )

  val nameToBuildTool = Map(
    "sbt" -> models.SBT,
    "maven" -> models.Maven,
    "gradle" -> models.Gradle
  )

  val nameToLanguage = Map(
    "scala" -> models.Scala,
    "java" -> models.Java
  )

  val buildToolGenerators: Map[BuildTool, Generator] =
    Map(models.Maven -> MvnGenerator, models.Gradle -> GradleGenerator)

  val projectTypes = List(models.Akka, models.Play, models.SimpleScala, models.Spark)
  val buildTools = List(models.SBT, models.Maven, models.Gradle)

  def index = Action {
    Ok(views.html.index(projectTypes, buildTools))
  }

  def generate(project: String, language: String, buildTool: String, name: String, organization: String) = Action.async { _ =>
    val errorOrDesc = for {
      proj <- findOrElse(project, nameToProjectType, s"Unknown project type $project").right
      lang <- findOrElse(language, nameToLanguage, s"Unknown language $language").right
      tool <- findOrElse(buildTool, nameToBuildTool, s"Unknown build tool $buildTool").right
      _ <- proj.validateCombination(tool, lang).toLeft(()).right
    } yield {
        ProjectDescription(proj, tool, lang, name, organization)
      }

    errorOrDesc.fold(
      error => Future.successful(BadRequest(error)),
      desc => buildToolGenerators(desc.buildTool).generate(desc).map { file =>
        Ok.sendFile(
          file,
          fileName = _ => desc.projectType.dirName + ".zip",
          onClose = () => file.delete()
        )
      }
    )
  }

  def findOrElse[T](key: String, map: Map[String, T], errorMsg: => String): Either[String, T] =
    map.get(key.toLowerCase).fold[Either[String, T]](Left(errorMsg))(Right(_))
}

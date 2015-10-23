package controllers

import java.io.File

import generators.{ Generator, MvnGenerator, SbtGenerator, GradleGenerator }
import models.{ Feature, ProjectType, BuildTool, ProjectDescription }
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.immutable.Seq

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
    Map(
      models.Maven -> MvnGenerator,
      models.Gradle -> GradleGenerator,
      models.SBT -> SbtGenerator
    )

  val projectTypes = List(models.Akka, models.Play, models.SimpleScala, models.Spark)
  val buildTools = List(models.SBT, models.Maven, models.Gradle)

  def index = Action {
    Ok(views.html.index(projectTypes, buildTools))
  }

  def generate(project: String, language: String, buildTool: String, name: String, organization: String) = Action.async { request =>
    val errorOrDesc: Either[String, ProjectDescription] =
      for {
        projectType <- findOrElse(project, nameToProjectType, s"Unknown project type $project").right
        lang <- findOrElse(language, nameToLanguage, s"Unknown language $language").right
        tool <- findOrElse(buildTool, nameToBuildTool, s"Unknown build tool $buildTool").right
        _ <- projectType.validateCombination(tool, lang).toLeft(()).right
        features <- toFeatures(featureNamesFrom(request), projectType).right
        project <- enableFeatures(ProjectDescription(projectType, tool, lang, name, organization), features).right
      } yield project

    errorOrDesc.fold(
      error => Future.successful(BadRequest(error)),
      desc => generateProjectZip(desc).map { file =>
        Ok.sendFile(
          file,
          fileName = _ => desc.projectType.dirName + ".zip",
          onClose = () => file.delete()
        )
      }
    )
  }

  // blocking op, so run it on its own dispatcher
  private def generateProjectZip(desc: ProjectDescription): Future[File] = Future {
    buildToolGenerators(desc.buildTool).generate(desc)
  }(_root_.global.Dispatchers.ioDispatcher)

  private def findOrElse[T](key: String, map: Map[String, T], errorMsg: => String): Either[String, T] =
    map.get(key.toLowerCase).fold[Either[String, T]](Left(errorMsg))(Right(_))

  private def toFeatures(names: Seq[String], projectType: ProjectType): Either[String, List[Feature]] =
    names.foldLeft[Either[String, List[Feature]]](Right(Nil)) { (acc, name) =>
      acc match {
        case Right(features) =>
          projectType.featureChoices.find(_.name.toLowerCase == name.toLowerCase)
            .toRight(s"Unknown feature name '$name'")
            .right.map(feature => feature :: features)
        case l => l
      }
    }

  private def enableFeatures(project: ProjectDescription, features: List[Feature]): Either[String, ProjectDescription] =
    features.foldLeft[Either[String, ProjectDescription]](Right(project)) { (project, feature) =>
      project match {
        case Right(p) => feature.enable(p)
        case l => l
      }
    }

  private def featureNamesFrom(request: Request[_]): List[String] =
    request.queryString.get("features").fold[List[String]](Nil)(_.flatMap(_.split(',')).toList)

}

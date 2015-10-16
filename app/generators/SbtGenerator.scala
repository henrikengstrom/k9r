package generators

import java.io.File
import java.nio.file.{Files, Paths, StandardOpenOption}

import models._
import play.twirl.api.Content

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SbtGenerator extends Generator {

  override def generate(projectDescription: ProjectDescription): Future[File] = {
    for {
      directory <- makeProjectBase(projectDescription.projectType)
      toZip <- generateSbtFiles(projectDescription, directory)
      zipped <- zip(toZip, projectDescription.projectType.dirName)
    } yield zipped
  }

  def generateSbtFiles(projectDescription: ProjectDescription, directory: File): Future[File] =
    projectDescription.projectType match {
      case Play => generatePlaySbtFiles(projectDescription, directory)
      case Akka => generateAkkaSbtFiles(projectDescription, directory)
      case Spark => generateSparkSbtFiles(projectDescription, directory)
      case SimpleScala => generateScalaSbtFiles(projectDescription, directory)
    }

  def generatePlaySbtFiles(projectDescription: ProjectDescription, directory: File): Future[File] = {
    projectDescription.language match {
      case Scala =>
        renderToFile(sbt.txt.playscalabuildsbt(projectDescription), directory, "build.sbt")
        renderToFile(sbt.txt.playscalapluginssbt(), new File(directory, "project"), "plugins.sbt")
      case Java =>
        renderToFile(sbt.txt.playjavabuildsbt(projectDescription), directory, "build.sbt")
        renderToFile(sbt.txt.playjavapluginssbt(), new File(directory, "project"), "plugins.sbt")
    }
    createSourceDirs(projectDescription, directory)
    Future(directory)
  }

  def generateAkkaSbtFiles(projectDescription: ProjectDescription, directory: File): Future[File] = {
    renderToFile(sbt.txt.akkabuildsbt(projectDescription), directory, "build.sbt")
    createSourceDirs(projectDescription, directory)
    Future(directory)
  }

  def generateSparkSbtFiles(projectDescription: ProjectDescription, directory: File): Future[File] = {
    // TODO Java or Scala??
    renderToFile(sbt.txt.sparkbuildsbt(projectDescription), directory, "build.sbt")
    createSourceDirs(projectDescription, directory)
    Future(directory)
  }

  def generateScalaSbtFiles(projectDescription: ProjectDescription, directory: File): Future[File] = {
    renderToFile(sbt.txt.scalabuildsbt(projectDescription), directory, "build.sbt")
    createSourceDirs(projectDescription, directory)
    Future(directory)
  }

  def createSourceDirs(projectDescription: ProjectDescription, directory: File): Unit = {
    val path = directory.toPath.toString
    new File(s"$path/src/main/${projectDescription.language.languageName}/" + projectDescription.organization.replace(".", "/")).mkdirs()
    new File(s"$path/src/test/${projectDescription.language.languageName}/" + projectDescription.organization.replace(".", "/")).mkdirs()
  }

  /**
   * Utility method used in the template, will generate dependency list
   * @param projectDescription
   * @return String
   */
  def generateAkkaDeps(projectDescription: ProjectDescription): String = {
    val start = "Seq("
    val end = ")"
    // TODO this needs to come from somewhere else..
    val scalaAdditionalDeps = List(Dependency("org.scalatest", "scalatest", "2.2.4", Some("test")))
    val javaAdditionalDeps = List(Dependency("junit", "junit", "4.12", Some("test"), false),
      Dependency("com.novocode", "junit-interface", "0.11", Some("test"), false))

    projectDescription.language match {
      case Scala => start + (projectDescription.projectType.dependencies ++ scalaAdditionalDeps).map(formatDep).mkString(",\n") + end
      case Java => start + (projectDescription.projectType.dependencies ++ javaAdditionalDeps).map(formatDep).mkString(",\n") + end
    }
  }


  def formatDep(dep: Dependency) = {
    val scopeString = dep.scope.fold("")(s => s""" % "$s"""")
    val seperator = if (dep.addScalaVersion) "%%" else "%"
    s""""${dep.groupId}" ${seperator} "${dep.artifactId}" % "${dep.version}"${scopeString}"""
  }


  def renderToFile(content: Content, directory: File, filename: String): Unit =
  // TODO there must be a better way to write a string to a file..
    Files.write(
      Paths.get((new File(directory, filename)).toURI),
      content.body.getBytes("utf-8"),
      StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

}

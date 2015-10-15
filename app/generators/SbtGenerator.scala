package generators

import java.io.File
import java.nio.file.{StandardOpenOption, Paths, Files}

import models._
import play.twirl.api.Content

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SbtGenerator extends Generator {
  override def generate(projectDescription: ProjectDescription): Future[File] = {
    for {
     directory <- makeProjectBase(projectDescription)
      _ <- generateSbtFiles(projectDescription, directory)
    } yield directory
  }

  def generateSbtFiles(projectDescription: ProjectDescription, directory: File): Future[File] =
    projectDescription.projectType match {
      case Play => ???
      case Akka => ???
      case Spark => ???
      case SimpleScala => generateScalaSbtFiles(projectDescription, directory)
    }



  def generateScalaSbtFiles(projectDescription: ProjectDescription, directory: File): Future[File] = {
      renderToFile(txt.scalabuildsbt(projectDescription), directory, "build.sbt")
    Future(directory)
  }

  def renderToFile(content: Content, directory: File, filename: String): Unit =
  // TODO there must be a better way to write a string to a file..
    Files.write(
      Paths.get((new File(directory, filename)).toURI),
      content.body.getBytes("utf-8"),
      StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)


  override def zip(file: File): Future[File] = Future(file)
}

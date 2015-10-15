package generators

import java.io.File

import models._

import scala.concurrent.Future

trait Generator {
  def generate(projectDescription: ProjectDescription): Future[File]

  def makeProjectBase(projectType: String): Future[File]

  def zip(file: File): Future[File]
}

object Generator {
  final val Standard = "standard"
  final val Play = "play"
}
package generators

import java.io.File

import models._

import scala.concurrent.Future

trait Generator {
  def generate(projectDescription: ProjectDescription): Future[File]
}

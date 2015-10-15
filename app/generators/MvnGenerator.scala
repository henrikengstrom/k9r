package generators

import java.io.File

import models.ProjectDescription

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

class MvnGenerator extends Generator {
  def generate(projectDescription: ProjectDescription): Future[File] = {
    makeProjectBase(projectDescription.projectType) map { folder: File =>
      val rendered = xml.pom.render(projectDescription.organization, projectDescription.name)
      rendered.body
      folder
    }
  }
}

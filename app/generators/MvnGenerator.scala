package generators

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import models.ProjectDescription

import scala.concurrent.{ ExecutionContext, Future }

object MvnGenerator extends Generator {

  def generate(projectDescription: ProjectDescription)(implicit ec: ExecutionContext): Future[File] = {
    makeProjectBase(projectDescription.projectType) flatMap { folder =>
      val pomXmlContent: String = xml.pom.render(projectDescription).body

      val pomXmlFile = new File(folder, "pom.xml")
      Files.write(pomXmlFile.toPath, pomXmlContent.getBytes(StandardCharsets.UTF_8))

      val path = folder.toPath.toString
      new File(s"$path/src/main/${projectDescription.language.languageName}/" + projectDescription.organization.replace(".", "/") + "/" + projectDescription.name).mkdirs()
      new File(s"$path/src/test/${projectDescription.language.languageName}/" + projectDescription.organization.replace(".", "/") + "/" + projectDescription.name).mkdirs()

      val generators = projectDescription.projectType.sampleCodeGenerators(projectDescription.language)
      generators.foreach(_.generateCode(projectDescription, folder.toPath))

      zip(folder, projectDescription.projectType.dirName)
    }
  }
}

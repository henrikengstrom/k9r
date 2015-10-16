package generators

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import models.ProjectDescription

import scala.concurrent.Future


object MvnGenerator extends Generator {

  import global.Dispatchers.ioDispatcher

  def generate(projectDescription: ProjectDescription): Future[File] = {
    makeProjectBase(projectDescription.projectType) flatMap { folder =>
      val pomXmlContent: String = xml.pom.render(projectDescription).body

      // TODO : truncate all returns in the dependencies section -> beautify

      val pomXmlFile = new File(folder, "pom.xml")
      Files.write(pomXmlFile.toPath, pomXmlContent.getBytes(StandardCharsets.UTF_8))

      val path = folder.toPath.toString
      new File(s"$path/src/main/${projectDescription.language.languageName}/" + projectDescription.organization.replace(".", "/") + s"/${projectDescription.name}/").mkdirs()
      new File(s"$path/src/test/${projectDescription.language.languageName}/" + projectDescription.organization.replace(".", "/") + s"/${projectDescription.name}/").mkdirs()

      zip(folder, projectDescription.projectType.dirName)
    }
  }
}

package generators

import models.ProjectDescription
import scala.concurrent.{ ExecutionContext, Future }
import java.io.File
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import models.ProjectDescription

object GradleGenerator extends Generator {

  def generate(projectDescription: ProjectDescription)(implicit ec: ExecutionContext): Future[File] = {
    makeProjectBase(projectDescription.projectType).map { folder =>
      val gradleBuildContent: String = txt.gradle_build.render(projectDescription).body
      val gradleBuildFile = folder.toPath().resolve("build.gradle")
      Files.write(gradleBuildFile, gradleBuildContent.getBytes(StandardCharsets.UTF_8))

      List("main", "test").map { s =>
        folder.toPath()
          .resolve("src")
          .resolve(s)
          .resolve(projectDescription.language.languageName)
          .resolve(projectDescription.organization.replace(".", "/"))
          .resolve(projectDescription.name.toLowerCase())
      }.foreach { _.toFile().mkdirs() }
      folder
    }.flatMap {
      zip(_, projectDescription.projectType.dirName)
    }
  }
}
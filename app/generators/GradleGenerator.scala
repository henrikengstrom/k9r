package generators

import models.ProjectDescription
import scala.concurrent.{ ExecutionContext, Future }
import java.io.File
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import models.ProjectDescription
import models.ProjectDescription
import java.nio.file.Path

object GradleGenerator extends Generator {

  def generate(projectDescription: ProjectDescription)(implicit ec: ExecutionContext): Future[File] = {
    makeProjectBase(projectDescription.projectType).map { folder =>

      val root = folder.toPath()

      CodeGenerator.mainPackagePath(root, projectDescription).toFile().mkdirs()
      CodeGenerator.testPackagePath(root, projectDescription).toFile().mkdirs()

      val gradleBuildContent: String = txt.gradle_build.render(projectDescription).body
      val gradleBuildFile = root.resolve("build.gradle")
      Files.write(gradleBuildFile, gradleBuildContent.getBytes(StandardCharsets.UTF_8))

      projectDescription.projectType.sampleCodeGenerators(projectDescription.language).foreach { g =>
        g.generateCode(projectDescription, root)
      }

      folder
    }.flatMap {
      zip(_, projectDescription.projectType.dirName)
    }
  }
}

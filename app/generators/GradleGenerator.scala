package generators

import java.io.File
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import models.ProjectDescription

object GradleGenerator extends Generator {

  def generate(projectDescription: ProjectDescription): File = {
    val folder = makeProjectBase(projectDescription.projectType)

    val root = folder.toPath()

    CodeGenerator.mainPackagePath(root, projectDescription).toFile().mkdirs()
    CodeGenerator.testPackagePath(root, projectDescription).toFile().mkdirs()

    val gradleBuildContent: String = txt.gradle_build.render(projectDescription).body
    val gradleBuildFile = root.resolve("build.gradle")
    Files.write(gradleBuildFile, gradleBuildContent.getBytes(StandardCharsets.UTF_8))

    projectDescription.projectType.sampleCodeGenerators(projectDescription.language).foreach { g =>
      g.generateCode(projectDescription, root)
    }

    zip(folder, projectDescription.projectType.dirName)
  }
}

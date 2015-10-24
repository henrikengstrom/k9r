package generators

import java.io.File
import java.nio.file.{ Files, Paths, StandardOpenOption }

import models._
import play.twirl.api.Content

object SbtGenerator extends Generator {

  override def generate(projectDescription: ProjectDescription) = {
    val directory = makeProjectBase(projectDescription.projectType)
    val withCommonfiles = createCommonFiles(projectDescription, directory)
    val withSbt = generateSbtFiles(projectDescription, withCommonfiles)
    generateCodeFiles(projectDescription, directory)
    val zipped = zip(withSbt, projectDescription.projectType.dirName)
    zipped
  }

  def generateSbtFiles(projectDescription: ProjectDescription, directory: File): File = {
    projectDescription.projectType match {
      case Play => generatePlaySbtFiles(projectDescription, directory)
      case Akka => generateAkkaSbtFiles(projectDescription, directory)
      case Spark => generateSparkSbtFiles(projectDescription, directory)
      case SimpleScala => generateScalaSbtFiles(projectDescription, directory)
    }
  }

  def generateCodeFiles(projectDescription: ProjectDescription, folder: File): Unit = {
    val generators = projectDescription.projectType.sampleCodeGenerators(projectDescription.language)
    generators.foreach(_.generateCode(projectDescription, folder.toPath))
  }

  def generatePlaySbtFiles(projectDescription: ProjectDescription, directory: File): File = {
    projectDescription.language match {
      case Scala =>
        val buildSbt = renderToFile(sbt.txt.playscalabuildsbt(projectDescription), directory, "build.sbt")
        renderToFile(sbt.txt.playscalapluginssbt(), new File(directory, "project"), "plugins.sbt")
        buildSbt

      case Java =>
        val buildSbt = renderToFile(sbt.txt.playjavabuildsbt(projectDescription), directory, "build.sbt")
        renderToFile(sbt.txt.playjavapluginssbt(), new File(directory, "project"), "plugins.sbt")
        buildSbt
    }
  }

  def generateAkkaSbtFiles(projectDescription: ProjectDescription, directory: File): File = {
    renderToFile(sbt.txt.akkabuildsbt(projectDescription), directory, "build.sbt")
  }

  def generateSparkSbtFiles(projectDescription: ProjectDescription, directory: File): File = {
    // TODO for now it's just Scala. Maybe Java or Scala??
    renderToFile(sbt.txt.sparkbuildsbt(projectDescription), directory, "build.sbt")
  }

  def generateScalaSbtFiles(projectDescription: ProjectDescription, directory: File): File = {
    renderToFile(sbt.txt.scalabuildsbt(projectDescription), directory, "build.sbt")
  }

  def createCommonFiles(projectDescription: ProjectDescription, directory: File): File = {

    if (projectDescription.projectType != models.Play) {
      CodeGenerator.mainPackagePath(directory.toPath, projectDescription).toFile().mkdirs()
      CodeGenerator.testPackagePath(directory.toPath, projectDescription).toFile().mkdirs()

      //new File(s"$path/src/main/${projectDescription.language.languageName}/" + projectDescription.organization.replace(".", "/") + "/" + projectDescription.name).mkdirs()
      //new File(s"$path/src/test/${projectDescription.language.languageName}/" + projectDescription.organization.replace(".", "/") + "/" + projectDescription.name).mkdirs()
    }
    //

    val sbtprojectsubdir = new File(directory, "project")
    if (!sbtprojectsubdir.exists()) sbtprojectsubdir.mkdir()

    Files.write(
      Paths.get(new File(sbtprojectsubdir, "build.properties").toURI),
      "sbt.version=0.13.9".getBytes("utf-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
    )
    // just return the same directory, for type signature convenience / composability
    directory
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
    val javaAdditionalDeps = List(
      Dependency("junit", "junit", "4.12", Some("test"), false),
      Dependency("com.novocode", "junit-interface", "0.11", Some("test"), false)
    )

    projectDescription.language match {
      case Scala => start + (projectDescription.dependencies ++ scalaAdditionalDeps).map(formatDep).mkString(",\n") + end
      case Java => start + (projectDescription.dependencies ++ javaAdditionalDeps).map(formatDep).mkString(",\n") + end
    }
  }

  /**
   * Utility method used in the template, will generate dependency list
   * @param projectDescription
   * @return String
   */
  def generateDeps(projectDescription: ProjectDescription): String = {
    val start = "Seq("
    val end = ")"
    start + projectDescription.dependencies.map(formatDep).mkString(",\n") + end
  }

  def formatDep(dep: Dependency) = {
    val scopeString = dep.scope.fold("")(s => s""" % "$s"""")
    val seperator = if (dep.addScalaVersion) "%%" else "%"
    s""""${dep.groupId}" ${seperator} "${dep.artifactId}" % "${dep.version}"${scopeString}"""
  }

  def renderToFile(content: Content, directory: File, filename: String): File = {
    // TODO there must be a better way to write a string to a file..
    Files.write(
      Paths.get((new File(directory, filename)).toURI),
      content.body.getBytes("utf-8"),
      StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
    )
    // just return the same directory, for type signature convenience / composability
    directory
  }
}

package models

import generators.CodeGenerator
import models.Feature

case class ProjectDescription(
    projectType: ProjectType,
    buildTool: BuildTool,
    language: Language,
    name: String,
    organization: String,
    customDependencies: List[Dependency] = Nil,
    selectedOptions: Set[String] = Set.empty) {

  def dependencies: List[Dependency] = projectType.dependencies ::: customDependencies

  def withDependency(dependency: Dependency) =
    if (dependencies.contains(dependency)) this
    else copy(customDependencies = dependency :: customDependencies)

}

sealed trait ProjectType {
  def name: String
  def tagline: String
  def description: String
  def version: Option[String]

  def dirName: String

  def dependencies: List[Dependency]
  def supportedLanguages: Set[Language]
  def supportedBuildTools: Set[BuildTool]

  /** a set of option names for this kind of project, for the user to toggle */
  def featureChoices: List[Feature] = Nil

  /**
   * @return A description of what is wrong with the given tool and language for this
   *         project type or None if they are ok
   */
  def validateCombination(tool: BuildTool, language: Language): Option[String] = None

  def sampleCodeGenerators(language: Language): List[CodeGenerator]
}

case object Play extends ProjectType {
  def name = "Play"
  def tagline = "Create a reactive web application project"
  def description = "Play framework is a high velocity, modern, scalable and fun way to create web applications"
  def version = Some("2.4.3")

  def dirName: String = "play-webapp"

  def dependencies = Nil

  override def validateCombination(tool: BuildTool, language: Language): Option[String] =
    if (tool != SBT) Some("Play project can only be created with SBT")
    else None

  def supportedLanguages: Set[Language] = Set(Scala, Java)
  def supportedBuildTools: Set[BuildTool] = Set(SBT)

  def sampleCodeGenerators(language: Language): List[CodeGenerator] = Nil
}

case object Akka extends ProjectType {
  def name = "Akka"
  def tagline = "Concurrent and scalable apps with the actor model"
  def description = "Akka is a library for building reactive applications based on the actor model"

  val akkaVersion = "2.4.0"

  def version = Some(akkaVersion)

  def dirName: String = "akka-project"

  def dependencies =
    List(
      Dependency("com.typesafe.akka", "akka-actor", akkaVersion, addScalaVersion = true),
      Dependency("com.typesafe.akka", "akka-testkit", akkaVersion, Some("test"), addScalaVersion = true)
    )

  def supportedLanguages: Set[Language] = Set(Scala, Java)
  def supportedBuildTools: Set[BuildTool] = Set(SBT, Maven, Gradle)

  def sampleCodeGenerators(language: Language): List[CodeGenerator] = Nil

  private val akkaCluster = Dependency("com.typesafe.akka", "akka-cluster", akkaVersion, addScalaVersion = true)

  override def featureChoices = List(
    Feature(
      "Clustering",
      "Run Akka across multiple connected nodes",
      project => Right(project.withDependency(akkaCluster))
    ),
    Feature(
      "Cluster Sharding",
      "Shard actors across your akka cluster",
      project => Right(project.withDependency(akkaCluster)
        .withDependency(Dependency("com.typesafe.akka", "akka-cluster-sharding", akkaVersion, addScalaVersion = true)))
    ),
    Feature(
      "Distributed Data",
      "Eventually consistent distributed data across your akka cluster",
      project => Right(project.withDependency(akkaCluster)
        .withDependency(Dependency("com.typesafe.akka", "akka-distributed-data-experimental", akkaVersion, addScalaVersion = true)))
    ),
    Feature(
      "Persistence",
      "Make actor state persistent",
      project => Right(project.withDependency(akkaCluster)
        .withDependency(Dependency("com.typesafe.akka", "akka-persistence", akkaVersion, addScalaVersion = true)))
    )
  )

}

case object Spark extends ProjectType {
  def name = "Spark"
  def tagline = "High speed processing of your big data"
  def description = "Spark is a fast and very general engine for large-scale data processing"
  def version = Some("1.5.1")

  def dirName: String = "spark-project"

  def dependencies = Nil

  def supportedLanguages: Set[Language] = Set(Scala, Java)
  def supportedBuildTools: Set[BuildTool] = Set(SBT, Maven, Gradle)

  def sampleCodeGenerators(language: Language): List[CodeGenerator] = Nil

  def addOption(project: ProjectDescription, optionName: String): Either[String, ProjectDescription] =
    Left(s"Option $optionName not supported for Spark projects")
}

case object SimpleScala extends ProjectType {
  def name = "Scala"
  def tagline = "Minimal project skeleton for a pure Scala application"
  def description = "Scala is a modern, concise and type safe language that mixes functional programming with OO"
  def version = None

  def dirName: String = "scala-project"

  // plain Scala gets scalatest as a default dependency. this is in line with the hello-scala activator template
  def dependencies = List(Dependency("org.scalatest", "scalatest", "2.2.4", Some("test")))

  override def validateCombination(tool: BuildTool, language: Language): Option[String] =
    if (language != Scala) Some("Scala language is the only language for the simple Scala project (well duh!)")
    else None

  def supportedLanguages: Set[Language] = Set(Scala)
  def supportedBuildTools: Set[BuildTool] = Set(SBT, Maven, Gradle)

  def sampleCodeGenerators(language: Language): List[CodeGenerator] = {
    if (language == Scala) {
      List(CodeGenerator.SimpleScalaMainScala)
    } else {
      Nil
    }
  }

  override def featureChoices = List(
    Feature(
      "Slick",
      "Connect to relational databases with Slick",
      project => Right(project.withDependency(Dependency("com.typesafe.slick", "slick", "3.1.0")))
    ))

}

sealed trait BuildTool {
  def name: String
  def description: String
  def requirements: String
}
case object SBT extends BuildTool {
  def name = "SBT"
  def description = "The de facto build tool in Scala"
  def requirements = "You need to have Java 8 installed"
}
case object Maven extends BuildTool {
  def name = "Maven"
  def description = "A very common build tool in the Java world"
  def requirements = "You need to have Java and Maven installed"
}
case object Gradle extends BuildTool {
  def name = "Gradle"
  def description = "A popular build tool based on the Groovy programming language"
  def requirements = "You need to have Java and Gradle installed"
}

sealed trait Language {
  def version: String

  def languageName: String
}

case object Scala extends Language {
  def version = "2.11.6"
  def shortVersion = "2.11"

  def languageName: String = "scala"
}

case object Java extends Language {
  def version = "1.8"

  def languageName: String = "java"
}

case class Dependency(
  groupId: String,
  artifactId: String,
  version: String,
  scope: Option[String] = None,
  addScalaVersion: Boolean = true)

/**
 * @param enable Enable the feature for a given project, return Left(errormsg) if something went wrong,
 *               or Right(updated project) if everything is golden
 */
case class Feature(
  name: String,
  description: String,
  enable: ProjectDescription => Either[String, ProjectDescription])


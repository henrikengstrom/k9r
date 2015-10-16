package models

case class ProjectDescription(
  projectType: ProjectType,
  buildTool: BuildTool,
  language: Language,
  name: String,
  organization: String)

sealed trait ProjectType {
  def name: String
  def tagline: String
  def description: String
  def version: Option[String]

  def dirName: String

  def dependencies: List[Dependency]
  def supportedLanguages: Set[Language]
  def supportedBuildTools: Set[BuildTool]

  /**
   * @return A description of what is wrong with the given tool and language for this
   *         project type or None if they are ok
   */
  def validateCombination(tool: BuildTool, language: Language): Option[String] = None
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

}

case object Akka extends ProjectType {
  def name = "Akka"
  def tagline = "Concurrent and scalable apps with the actor model"
  def description = "Akka is a library for building reactive applications based on the actor model"
  def version = Some("2.4.0")

  def dirName: String = "akka-project"

  def dependencies =
    List(Dependency("com.typesafe.akka", "akka-actor", "2.4.0"), Dependency("com.typesafe.akka", "akka-testkit", "2.4.0", Some("test")))

  def supportedLanguages: Set[Language] = Set(Scala, Java)
  def supportedBuildTools: Set[BuildTool] = Set(SBT, Maven, Gradle)
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
}

case object SimpleScala extends ProjectType {
  def name = "Scala"
  def tagline = "Minimal project skeleton for a pure Scala application"
  def description = "Scala is a modern, concise and type safe language that mixes functional programming with OO"
  def version = None

  def dirName: String = "scala-project"

  def dependencies = Nil

  override def validateCombination(tool: BuildTool, language: Language): Option[String] =
    if (language != Scala) Some("Scala language is the only language for the simple Scala project (well duh!)")
    else None

  def supportedLanguages: Set[Language] = Set(Scala)
  def supportedBuildTools: Set[BuildTool] = Set(SBT, Maven, Gradle)
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

  def languageName: String = "scala"
}

case object Java extends Language {
  def version = "1.8"

  def languageName: String = "java"
}

case class Dependency(artifactId: String,
  groupId: String,
  version: String,
  scope: Option[String] = None,
  addScalaVersion: Boolean = true)

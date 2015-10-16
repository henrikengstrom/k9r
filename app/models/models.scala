package models

case class ProjectDescription(
    projectType: ProjectType,
    buildTool: BuildTool,
    language: Language,
    name: String,
    organization: String)

sealed trait ProjectType {
  def version: Option[String]
  def dirName: String
  def dependencies: List[Dependency]

  /**
   * @return A description of what is wrong with the given tool and language for this
   *         project type or None if they are ok
   */
  def validateCombination(tool: BuildTool, language: Language): Option[String] = None
}

case object Play extends ProjectType {
  def version = Some("2.4.3")
  def dirName: String = "play-webapp"
  def dependencies = Nil

  override def validateCombination(tool: BuildTool, language: Language): Option[String] =
    if (tool != SBT) Some("Play project can only be created with SBT")
    else None
  
}
case object Akka extends ProjectType {
  def version = Some("2.4.0")
  def dirName: String = "akka-project"
  def dependencies =
    List(Dependency("com.typesafe.akka", "akka-actor_2.11", "2.4.0"))
}
case object Spark extends ProjectType {
  def version = Some("1.5.1")
  def dirName: String = "spark-project"
  def dependencies = Nil
}
case object SimpleScala extends ProjectType {
  def version = None
  def dirName: String = "scala-project"
  def dependencies = Nil

  override def validateCombination(tool: BuildTool, language: Language): Option[String] =
    if (language != Scala) Some("Scala language is the only language for the simple Scala project (well duh!)")
    else None
}

sealed trait BuildTool
case object SBT extends BuildTool
case object Maven extends BuildTool
case object Gradle extends BuildTool

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

case class Dependency(artifactId: String, groupId: String, version: String)
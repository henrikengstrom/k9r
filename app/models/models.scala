package models

case class ProjectDescription(
    projectType: ProjectType,
    buildTool: BuildTool,
    language: Language,
    name: String,
    organization: String)

sealed trait ProjectType {
  def version: Option[String]
  def dependencies: List[Dependency]
}

case object Play extends ProjectType {
  def version = Some("2.4.3")
  def dependencies = Nil
}
case object Akka extends ProjectType {
  def version = Some("2.4.0")
  def dependencies = Nil
}
case object Spark extends ProjectType {
  def version = Some("1.5.1")
  def dependencies = Nil
}
case object SimpleScala extends ProjectType {
  def version = None
  def dependencies = Nil
}

sealed trait BuildTool
case object SBT extends BuildTool
case object Maven extends BuildTool
case object Gradle extends BuildTool

sealed trait Language {
  def version: String
}
case object Scala extends Language {
  def version = "2.11.6"
}
case object Java extends Language {
  def version = "1.8"
}

case class Dependency(artifactId: String, groupId: String, version: String)
name := """k9r"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

sourceDirectories in (Compile, TwirlKeys.compileTemplates) += baseDirectory.value / "templates"

scalaVersion := "2.11.6"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.4")

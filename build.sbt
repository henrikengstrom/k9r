name := """k9r"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// Includes templates in the compilation
sourceDirectories in (Compile, TwirlKeys.compileTemplates) += baseDirectory.value / "templates"

// Adds resources to dist
import NativePackagerHelper._
mappings in Universal ++= contentOf(baseDirectory.value / "resources")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
import java.io.File

import generators.SbtGenerator
import models._
import org.scalatest._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class SbtGeneratorSpec extends FunSpec with Matchers {

  val testDescription = ProjectDescription(SimpleScala, SBT, Scala, "test", "com.example")

  describe("Sbt rendered build.sbt") {
    it("should contain properties") {
      val rendered = sbt.txt.scalabuildsbt(testDescription).body
      rendered should include(testDescription.organization)
      rendered should include(testDescription.name)

    }
  }

  describe("Sbt Generator") {
    it("should generate File") {

      val tmpdir = new File(System.getProperty("java.io.tmpdir"))
      val dirWithFiles = SbtGenerator.generateSbtFiles(testDescription, tmpdir)

      System.out.println(s"Wrote to ${dirWithFiles.getCanonicalPath}")
      assert(new File(dirWithFiles, "build.sbt").exists())
    }

  }

  describe("Dependency formatter") {
    it("should format (1)") {
      val dep1 = Dependency("a", "b", "c")
      SbtGenerator.formatDep(dep1) should equal(""""a" %% "b" % "c"""")
    }
    it("should format (2)") {
      val dep1 = Dependency("a", "b", "c", None, false)
      SbtGenerator.formatDep(dep1) should equal(""""a" % "b" % "c"""")
    }
    it("should format (3)") {
      val dep1 = Dependency("a", "b", "c", Some("test"))
      SbtGenerator.formatDep(dep1) should equal(""""a" %% "b" % "c" % "test"""")
    }
    it("should format (4)") {
      val dep1 = Dependency("a", "b", "c", Some("test"), false)
      SbtGenerator.formatDep(dep1) should equal(""""a" % "b" % "c" % "test"""")
    }

  }

}
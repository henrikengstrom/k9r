import java.io.File

import generators.SbtGenerator
import models.{SBT, SimpleScala, Scala, ProjectDescription}
import org.scalatest._

import scala.concurrent._
import scala.concurrent.duration._

class SbtGeneratorSpec extends FunSpec with Matchers {

  val testDescription = ProjectDescription(SimpleScala, SBT, Scala, "test", "com.example")

  describe("Sbt rendered build.sbt") {
    it("should contain properties") {
      val rendered = txt.scalabuildsbt(testDescription).body
      rendered should include (testDescription.organization)
      rendered should include (testDescription.name)

    }
  }

  describe("Sbt Generator") {
      it("should generate File") {

        val generator = new SbtGenerator
        val tmpdir = new File(System.getProperty("java.io.tmpdir"))
        val f = generator.generateSbtFiles(testDescription, tmpdir)

        val dirWithFiles = Await.result(f, 30 seconds)
        System.out.println(s"Wrote to ${dirWithFiles.getCanonicalPath}")
        assert(new File(dirWithFiles, "build.sbt").exists())
      }

  }
}
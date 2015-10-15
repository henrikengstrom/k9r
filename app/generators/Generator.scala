package generators

import java.io.File
import java.nio.file.FileSystems

import models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

trait Generator {
  val currentPath = new File(".").getCanonicalFile.getCanonicalPath
  
  def generate(projectDescription: ProjectDescription): Future[File]

  def makeProjectBase(projectType: String): Future[File] = {
    def randomName: String = s"tmp-${System.currentTimeMillis}-${Random.nextLong}-k9r"

    Future {
      val fileSystem = FileSystems.getDefault
      val source =  fileSystem.getPath(currentPath, "resources")
      val target = fileSystem.getPath(currentPath, randomName)
      // COPY THE SHIZZLE
      target.toFile
    }
  }

  def zip(file: File): Future[File]
}

object Generator {
  final val Standard = "resources-standard"
  final val Play = "resources-play"
}
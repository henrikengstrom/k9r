package generators

import models._

import java.io.File
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import scala.concurrent.Future
import scala.util.Random

trait Generator {
  import Generator._

  import scala.concurrent.ExecutionContext.Implicits.global

  def generate(projectDescription: ProjectDescription): Future[File]

  def makeProjectBase(projectType: String): Future[File] = {
    def randomName: String = s"tmp-${System.currentTimeMillis}-${Random.nextLong}-k9r"

    Future {
      val fileSystem = FileSystems.getDefault
      val source =  fileSystem.getPath(currentPath, projectType)
      val target = fileSystem.getPath(currentPath, randomName)
      copyDir(source, target)
      target.toFile
    }
  }

  def zip(file: File): Future[File]
}

object Generator {
  final val Standard = "resources-standard"
  final val Play = "resources-play"

  val currentPath = new File(".").getCanonicalFile.getCanonicalPath

  def copyDir(src: Path, dest: Path) {
    val fileVisitor = new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult = {
        copy(file)
        FileVisitResult.CONTINUE
      }
      override def preVisitDirectory(file: Path, attributes: BasicFileAttributes): FileVisitResult = {
        copy(file)
        FileVisitResult.CONTINUE
      }

      private def copy(file: Path) {
        val pathInDest = dest.resolve(src.relativize(file).toString)
        Files.copy(file, pathInDest, StandardCopyOption.REPLACE_EXISTING)
      }
    }

    Files.walkFileTree(src, fileVisitor)
  }
}

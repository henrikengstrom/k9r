package generators

import models._

import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

import scala.concurrent.Future

trait Generator {
  def generate(projectDescription: ProjectDescription): Future[File]

  def makeProjectBase(projectType: String): Future[File]

  def zip(file: File): Future[File]
}

object Generator {
  final val Standard = "standard"
  final val Play = "play"

  def copyDir(src: Path, dest: Path) {
    val fileVisitor = new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        copy(file)
        FileVisitResult.CONTINUE
      }
      override def preVisitDirectory(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        copy(file)
        FileVisitResult.CONTINUE
      }

      private def copy(file: Path) {
        val pathInDest = dest.resolve(src.relativize(file).toString())
        Files.copy(file, pathInDest, StandardCopyOption.REPLACE_EXISTING);
      }
    }

    Files.walkFileTree(src, fileVisitor)
  }
}

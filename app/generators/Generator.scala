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

  def makeProjectBase(projectType: ProjectType): Future[File] = {
    def randomName: String = s"tmp-${System.currentTimeMillis}-${Random.nextLong}-k9r"

    def mapProjectType(pt: ProjectType): String = pt match {
      case models.Play => PlayResources
      case _ => StandardResources
    }
    
    Future {
      val fileSystem = FileSystems.getDefault
      val source =  fileSystem.getPath(currentPath, mapProjectType(projectType))
      val target = fileSystem.getPath(currentPath, randomName)
      copyDir(source, target, Some(k9rFileEnding))
      target.toFile
    }
  }

  def zip(file: File): Future[File] = ???
}

object Generator {
  final val StandardResources = "resources-standard"
  final val PlayResources = "resources-play"
  final val k9rFileEnding = ".k9r"

  val currentPath = new File(".").getCanonicalFile.getCanonicalPath

  def copyDir(src: Path, dest: Path, filterOut: Option[String] = None) {
    val fileVisitor = new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult = {
        if (filterOut.isDefined && file.getFileName.endsWith(filterOut.get))
          Files.delete(file)
        else
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

package generators

import models._
import java.io.File
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import scala.concurrent.Future
import scala.util.Random
import java.util.{ HashMap => jHashMap }
import java.net.URI
import java.io.IOException

trait Generator {
  import Generator._

  import scala.concurrent.ExecutionContext.Implicits.global

  def generate(projectDescription: ProjectDescription): Future[File]

  def makeProjectBase(projectType: String): Future[File] = {
    Future {
      val source =  currentPath.resolve(projectType)
      val target = currentPath.resolve(randomName)
      copyDir(source, target)
      target.toFile
    }
  }

  /**
   * Zip the given folder, delete the folder and return the zip file. 
   */
  def zip(folder: File): Future[File] = {
    Future {
      val folderToZip = folder.toPath()

      val env = new jHashMap[String, String]()
      env.put("create", "true")

      val zipFile = currentPath.resolve(randomName)
      val zipFSURI = URI.create(s"jar:${zipFile.toUri().toString()}")
      val zipFS = FileSystems.newFileSystem(zipFSURI, env)

      try {
        val folderInZip = zipFS.getPath("/", folderToZip.getFileName.toString())

        Generator.copyDir(folderToZip, folderInZip)
      } finally {
        zipFS.close()
      }
      
      Generator.deleteDir(folderToZip)
      
      zipFile.toFile()
    }
  }
}

object Generator {
  final val Standard = "resources-standard"
  final val Play = "resources-play"

  val currentPath = Paths.get("").toAbsolutePath()

  def randomName: String = s"tmp-${System.currentTimeMillis}-${Random.nextLong}-k9r"

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
  
  def deleteDir(src: Path) {
    val fileVisitor = new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }
      override def postVisitDirectory(file: Path, ex: IOException): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }
    }

    Files.walkFileTree(src, fileVisitor)
  }
}

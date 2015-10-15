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

  def makeProjectBase(projectType: ProjectType): Future[File] = {
    def mapProjectType(pt: ProjectType): String = pt match {
      case models.Play => PlayResources
      case _ => StandardResources
    }
    
    Future {
      val source =  currentPath.resolve(mapProjectType(projectType))
      val target = currentPath.resolve(randomName)
      copyDir(source, target, Some(k9rFileEnding))
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
  final val StandardResources = "resources-standard"
  final val PlayResources = "resources-play"
  final val k9rFileEnding = ".k9r"

  val currentPath = Paths.get("").toAbsolutePath()

  def randomName: String = s"tmp-${System.currentTimeMillis}-${Random.nextLong}-k9r"

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

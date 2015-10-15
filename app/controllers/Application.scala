package controllers

import java.io.{PrintWriter, File}

import org.apache.commons.io._
import play.api._
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  def testPom = Action {
    /* the template will be compiled in Play's "normal"
     template compilation phase, and is available as Scala class then.
     In this case the template is pom.scala.xml in the templates folder,
     so it's available as "xml.pom".
     */
    val rendered = xml.pom.render("com.typesafe", "k9r")
    // we just write it into a temp directory
    val tempDir = System.getProperty("java.io.tmpdir")
    // because I'm lazy, using apache commons here.
    // There is no way to "stream" the rendered template to file,
    // you just get it as a String and write that out to a file.
    FileUtils.writeStringToFile(new File(tempDir, "pom.xml"), rendered.body)

    Ok
  }

}

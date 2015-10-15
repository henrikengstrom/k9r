package controllers

import play.api._
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def generate(project: String, buildTool: String, name: String, organization: String) = Action { _ =>

    Ok("TODO")
  }

}

package views

import models.{ BuildTool, ProjectType }
import play.api.libs.json.{ Json, Writes }

/**
 * Used to push the project type descriptions to json in the pages of the app
 */
object JsonFormats {

  implicit val writesProjectType = Writes[ProjectType] { pt =>
    Json.obj(
      "name" -> pt.name,
      "tagline" -> pt.tagline,
      "summary" -> pt.description,
      "buildTools" -> pt.supportedBuildTools.map(_.name),
      "languages" -> pt.supportedLanguages.map(_.languageName),
      "featureChoices" -> pt.featureChoices.map(feature => Json.obj("name" -> feature.name, "summary" -> feature.description))
    )
  }

  implicit val writesBuildTool = Writes[BuildTool] { tool =>
    Json.obj(
      "name" -> tool.name,
      "summary" -> tool.description,
      "requirements" -> tool.requirements
    )
  }

}
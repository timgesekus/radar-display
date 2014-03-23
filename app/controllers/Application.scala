package controllers

import play.api._
import play.api.mvc._
import play.api.Routes

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Events.events)).as("text/javascript")
  }

}
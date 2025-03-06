package controllers

import play.api.libs.json.Json
import play.api.mvc._
import play.filters.csrf.CSRF

import javax.inject._
//import scala.concurrent.ExecutionContext

@Singleton
class CSRFController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def csrfToken: Action[AnyContent] = Action { implicit request =>
    CSRF.getToken(request) match {
      case Some(token) => Ok(Json.obj("csrfToken" -> token.value))
      case None => Forbidden("No CSRF token available")
    }
  }
}


package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import javax.inject._
import play.api.mvc._
import daos.ItemDAO
import scala.concurrent.{ExecutionContext, Future}
import models.Item
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
//@Singleton
//class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
//  def index() = Action { implicit request: Request[AnyContent] =>
//    request.session.get("userId") match {
//      case Some(_) => {
//        itemDAO.getAllItems.map { items =>
//          Ok(views.html.index(items)) // ✅ Now passing items!
//        }
//      }
//        case None => Redirect(routes.UserController.logIn())
//      }
//    }
//  }




@Singleton
class HomeController @Inject()(
                                val controllerComponents: ControllerComponents,
                                itemDAO: ItemDAO
                              )(implicit ec: ExecutionContext) extends BaseController {

  def index() = Action.async { implicit request: Request[AnyContent] =>
    request.session.get("userId") match {
      case Some(_) =>
        itemDAO.getAllItems.map { items =>
          Ok(views.html.index(items))  // ✅ Now passing items!
        }
      case None =>
        Future.successful(Redirect(routes.UserController.logIn()))
    }
  }
}

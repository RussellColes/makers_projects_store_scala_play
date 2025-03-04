//// CAUTION: ALL code below is copied from UserController, do not run until all methods are amended to reflect item instead.

package controllers

import javax.inject._
import play.api.mvc._
import daos.ItemDAO
import models.Item
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import org.mindrot.jbcrypt.BCrypt

@Singleton
class ItemController @Inject()(cc: ControllerComponents, itemDAO: ItemDAO)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def findItemById(id: Long) = Action.async {
    implicit request: Request[AnyContent] =>
      itemDAO.findItemById(id).map {
        case Some(item) =>
          println(item)
          Ok(Json.toJson(item))
        case None =>
          NotFound(Json.obj("message" -> s"Item with id $id not found"))
      }
  }

}

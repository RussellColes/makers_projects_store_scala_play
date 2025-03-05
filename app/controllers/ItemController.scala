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

  def getAllItems() = Action.async {
    implicit  request: Request[AnyContent] =>
      itemDAO.getAllItems().map {items =>
        println(items)
//        Ok(Json.toJson(items))
        Ok(views.html.items(items))
      }
  }

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

//  def createItem(item: Item) = Action.async { implicit request: Request[AnyContent] =>
//
//    itemDAO.createItem(item).map {
//      case Some(item) =>
//        println(item)
//        Ok(Json.toJson(item))
//      case None =>
//        NotFound(Json.obj("message" -> s"Item with id $id not found"))
//    }

//  }
def createItem = Action.async(parse.json) { implicit request =>
  request.body.validate[Item].fold(
    errors => {
      Future.successful(BadRequest(Json.obj(
        "status" -> "error",
        "message" -> JsError.toJson(errors)
      )))
    },
    item => {
      itemDAO.createItem(item).map { createdId =>
        Created(Json.obj(
          "status" -> "success",
          "message" -> "Item successfully created",
          "id" -> createdId
        ))
      }.recover {
        case e: Exception =>
          InternalServerError(Json.obj(
            "status" -> "error",
            "message" -> s"Failed to create item: ${e.getMessage}"
          ))
      }
    }
  )
}

}

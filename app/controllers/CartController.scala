package controllers

import daos.{CartDAO, CartItemDAO}
import models.Cart

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

import java.sql.Timestamp
import java.time.Instant

@Singleton
class CartController @Inject()(cc: ControllerComponents, cartDAO: CartDAO, cartItemDAO: CartItemDAO)(implicit ec: ExecutionContext) extends AbstractController(cc)  {


  def createCart: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.session.get("userId") match {
      case Some(userIdStr) =>
        val userId = userIdStr.toLong
    }
      val json = request.body.as[JsObject]
//      val userId: Option[Long] = (json \ "userId").asOpt[Long]
      val now = Timestamp.from(Instant.now())
    
      val cart = Cart(None, userId, Some(now), Some(now))

      cartDAO.createCart(cart).map { createdCart =>
        Created(Json.obj("status" -> "success", "message" -> s"Cart ${createdCart.id.getOrElse("unknown")} created"))
      }.recover {
        case _ => InternalServerError(Json.obj("status" -> "error", "message" -> "Cart could not be created"))
      }
  }

  def getCartById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    cartDAO.findById(id).map {
      case Some(cart) => Ok(Json.toJson(cart))
      case None       => NotFound(Json.obj("error" -> s"Cart with id $id not found"))
    }
  }

  def getCartByUserId(userId: Long): Action[AnyContent] = Action.async { implicit request =>
    cartDAO.findByUserId(userId).map { carts =>
      if (carts.isEmpty)
        NotFound(Json.obj("error" -> s"No carts found for userId $userId"))
      else
        Ok(Json.toJson(carts))
    }
  }

  def updateCart(id: Long): Action[AnyContent] = ???

  def deleteCart(id: Long): Action[AnyContent] = ???

}

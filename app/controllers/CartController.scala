package controllers

import daos.{CartDAO, CartItemDAO}
import models.{Cart, CartItemView}

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

import java.sql.Timestamp
import java.time.Instant

@Singleton
class CartController @Inject()(cc: ControllerComponents, cartDAO: CartDAO)(implicit ec: ExecutionContext) extends AbstractController(cc)  {


  def createCart: Action[JsValue] = Action.async(parse.json) { implicit request =>
    println("Session contents: " + request.session.data)
    request.session.get("userId") match {
      case Some(userIdStr) =>
        val userId = userIdStr.toLong
        val now = Timestamp.from(Instant.now())
        val cart = Cart(None, Some(userId), Some(now), Some(now))

        cartDAO.createCart(cart).map { createdCart =>
          Created(Json.obj("status" -> "success", "message" -> s"Cart ${createdCart.id.getOrElse("unknown")} created"))
        }.recover {
          case ex =>
            InternalServerError(Json.obj("status" -> "error", "message" -> "Cart could not be created"))
        }
      case None =>
        Future.successful(Unauthorized(Json.obj("error" -> "User not logged in")))
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

//  ######CartItems logic#######
  def updateCartItem(itemId: Long) = Action { implicit request =>
    // TODO: Add logic to update the cart item quantity.
    Ok("Cart item updated")
  }

  def deleteCartItem(itemId: Long) = Action.async { implicit request =>
    // TODO: Replace with CartItemsDAO for deletion
    Future.successful(
      Redirect(routes.CartController.myCart())
        .flashing("success" -> s"Item $itemId deleted (mock).")
    )
  }


  //  ######View renders########
  def myCart() = Action { implicit request: Request[AnyContent] =>
    request.session.get("userId") match {
      case Some(_) =>
        // TODO: Replace with CartItems data
        val cartItems = Seq(
          CartItemView(1, "Product A", 2, 19.99),
          CartItemView(2, "Product B", 1, 9.99),
          CartItemView(3, "Product C", 3, 29.99)
        )
        Ok(views.html.mycart(cartItems))
      case None =>
        Redirect(routes.UserController.logIn())
    }
  }



}

package controllers

import daos.{CartDAO, CartItemDAO, CartItemViewDAO}
import models.{Cart, CartItem}

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

import java.sql.Timestamp
import java.time.Instant

@Singleton
class CartController @Inject()(cc: ControllerComponents, cartDAO: CartDAO, cartItemDAO: CartItemDAO, cartItemViewDAO: CartItemViewDAO)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // ######Option with json parsing for JS ################
  //  def addItemToCart: Action[JsValue] = Action.async(parse.json) { implicit request =>
  //    request.body.validate[CartItem].fold(
  //      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid item format"))),
  //      itemWithoutCartId => {
  //        request.session.get("userId") match {
  //          case Some(userIdStr) =>
  //            val userId = userIdStr.toLong
  //            cartDAO.findActiveCart(userId).flatMap {
  //              case Some(cart) =>
  //                // Ok, cart found, copying the existing cart id
  //                val itemWithCartId = itemWithoutCartId.copy(cartId = cart.id)
  //                cartItemDAO.createCartItem(itemWithCartId).map { createdItem =>
  //                  Ok(Json.obj(
  //                    "status" -> "Item added to existing cart",
  //                    "cartId" -> cart.id,
  //                    "cartItem" -> createdItem
  //                  ))
  //                }
  //              case None =>
  //                // No cart found, creating a new one
  //                val now = Timestamp.from(Instant.now())
  //                val newCart = Cart(None, Some(userId), "active", Some(now), Some(now))
  //                cartDAO.createCart(newCart).flatMap { newCart =>
  //                  val itemWithCartId = itemWithoutCartId.copy(cartId = newCart.id)
  //                  cartItemDAO.createCartItem(itemWithCartId).map { createdItem =>
  //                    Ok(Json.obj(
  //                      "status" -> "Item added to a new cart",
  //                      "cartId" -> newCart.id,
  //                      "cartItem" -> createdItem
  //                    ))
  //                  }
  //                }
  //            }
  //          case None =>
  //            Future.successful(BadRequest(Json.obj("error" -> "User not logged in")))
  //        }
  //      }
  //    )
  //  }

//  ######Option to parse Form Data #######
  def addItemToCart(itemId: Long) = Action.async { implicit request: Request[AnyContent] =>
    // TODO: Default quantity is 1. Need to discuss the logic
    val newCartItem = CartItem(id = None, cartId = None, itemId = Some(itemId), quantity = 1)
    request.session.get("userId") match {
      case Some(userIdStr) =>
        val userId = userIdStr.toLong
        cartDAO.findActiveCart(userId).flatMap {
          case Some(cart) =>
            val itemWithCartId = newCartItem.copy(cartId = cart.id)
            cartItemDAO.createCartItem(itemWithCartId).map { createdItem =>
              Redirect(routes.CartController.myCart())
            }
          case None =>
            val now = Timestamp.from(Instant.now())
            val newCart = Cart(id = None, userId = Some(userId), cartStatus = "active", createdAt = Some(now), updatedAt = Some(now))
            cartDAO.createCart(newCart).flatMap { createdCart =>
              val itemWithCartId = newCartItem.copy(cartId = createdCart.id)
              cartItemDAO.createCartItem(itemWithCartId).map { createdItem =>
                Redirect(routes.CartController.myCart())
              }
            }
        }
      case None =>
        Future.successful(Redirect(routes.UserController.logIn()))
    }
  }

  def cartCheckOut: Action[AnyContent] = Action.async { implicit request =>
    request.session.get("userId") match {
      case Some(userIdStr) =>
        val userId = userIdStr.toLong
        cartDAO.findActiveCart(userId).flatMap {
          case Some(cart) =>
            cart.id match {
              case Some(id) =>
                val originalCreateDate = cart.createdAt
                val now = Timestamp.from(Instant.now())
                val submittedCart = Cart(Some(id), Some(userId), "submitted", originalCreateDate, Some(now))
                cartDAO.updateCart(submittedCart).map { _ =>
                  Redirect(routes.HomeController.index())
                }
              case None =>
                Future.successful(BadRequest("Cart id is missing"))
            }
          case None =>
            Future.successful(BadRequest("Active cart not found"))
        }
      case None =>
        Future.successful(Redirect(routes.UserController.logIn()))
    }
  }


  //  def getCartById(id: Long): Action[AnyContent] = Action.async { implicit request =>
//    cartDAO.findById(id).map {
//      case Some(cart) => Ok(Json.toJson(cart))
//      case None       => NotFound(Json.obj("error" -> s"Cart with id $id not found"))
//    }
//  }

  def getCartByUserId(userId: Long): Action[AnyContent] = Action.async { implicit request =>
    cartDAO.findByUserId(userId).map { carts =>
      if (carts.isEmpty)
        NotFound(Json.obj("error" -> s"No carts found for userId $userId"))
      else
        Ok(Json.toJson(carts))
    }
  }


  //  ######CartItems logic#######
  def updateCartItem() = Action.async { implicit request =>
    request.body.asFormUrlEncoded match {
      case Some(formData) =>
        val idOpt = formData.get("id").flatMap(_.headOption)
        val quantityOpt = formData.get("quantity").flatMap(_.headOption)
        val updatedQuantity: Int = quantityOpt.map(_.toInt).getOrElse(1)

        idOpt match {
          case Some(idStr) =>
            val id = idStr.toLong
            cartItemDAO.findById(id).flatMap {
              case Some(existingCartItem) =>
                val updatedCartItem = existingCartItem.copy(quantity = updatedQuantity)
                cartItemDAO.updateCartItem(updatedCartItem).map { _ =>
                  Redirect(routes.CartController.myCart())
                }
              case None =>
                Future.successful(BadRequest("Cart item not found"))
            }
        }
          case None =>
            Future.successful(BadRequest("No cart item id provided"))
      case None =>
        Future.successful(Ok("No data received"))
    }
  }

  def deleteCartItem() = Action.async { implicit request =>
    request.body.asFormUrlEncoded match {
      case Some(formData) =>
        val idOpt = formData.get("id").flatMap(_.headOption)
        val id:Long = idOpt.getOrElse(
          throw new IllegalArgumentException("Cart item id is required")
        ).toLong
        cartItemDAO.deleteCartItem(id).map { _ =>
          Redirect(routes.CartController.myCart())
        }
      case None =>
        Future.successful(BadRequest("Cart item not found"))
    }
  }


  //  ######View render########

  def myCart() = Action.async { implicit request: Request[AnyContent] =>
    request.session.get("userId") match {
      case Some(userIdStr) =>
        val userId = userIdStr.toLong
        cartDAO.findActiveCart(userId).flatMap {
          case Some(cart) =>
            val actualCartId: Long = cart.id.getOrElse(
              throw new IllegalArgumentException("Cart Id is required")
            )
            cartItemViewDAO.findCartItemViews(actualCartId).map { cartItems =>
              Ok(views.html.mycart(cartItems))
            }
          case None =>
            Future.successful(Ok(views.html.mycart(Seq.empty)))
        }
      case None =>
        Future.successful(Redirect(routes.UserController.logIn()))
    }
  }


}

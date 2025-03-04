package daos

import models.{CartItem, CartItems}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartItemDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val cartItems = CartItems.table

  def createCartItem(cartItem: CartItem): Future[CartItem] = {
    val createQuery = (CartItems.table returning CartItems.table.map(_.id)
      into ((cart, id) => cart.copy(id = Some(id)))
      += cartItem)
    db.run(createQuery)
  }

  def findById(id: Long): Future[Option[CartItem]] = {
    val findByIdQuery = CartItems.table.filter(_.id === id).result.headOption
    db.run(findByIdQuery)
  }

  def findByCartId(cartId: Long): Future[Option[CartItem]] = {
    val findByUserIdQuery = CartItems.table.filter(_.cartId === cartId).result.headOption
    db.run(findByUserIdQuery)
  }

  def updateCartItem(cartItem: CartItem): Future[Int] = {
    cartItem.id match {
      case Some(id) =>
        val updateQuery = CartItems.table.filter(_.id === id).update(cartItem)
        db.run(updateQuery)
      case None =>
        Future.failed(new IllegalArgumentException("Please specify the cartItem id for update"))
    }
  }

  def deleteCartItem(id: Long): Future[Int] = {
    val deleteQuery = CartItems.table.filter(_.id === id).delete
    db.run(deleteQuery)
  }
}
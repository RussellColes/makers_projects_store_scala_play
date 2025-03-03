package daos

import models.{Cart, Carts}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  val carts = Carts.table

  // TBC: I'm expecting to see the cart instance in response instead of just an id. Hope it works ;)
  def createCart(cart: Cart): Future[Cart] = {
    val createQuery = (Carts.table returning Carts.table.map(_.id)
      into ((cart, id) => cart.copy(id = Some(id)))
      += cart)
    db.run(createQuery)
  }

  // Find cart by Id
  def findById(id: Long): Future[Option[Cart]] = {
    val findByIdQuery = Carts.table.filter(_.id === id).result.headOption
    db.run(findByIdQuery)
  }

  // Find cart by userId
  def findByUserId(userId: Long): Future[Option[Cart]] = {
    val findByUserIdQuery = Carts.table.filter(_.userId === userId).result.headOption
    db.run(findByUserIdQuery)
  }

  // Update cart
  def updateCart(cart: Cart): Future[Int] = {
    cart.id match {
      case Some(id) =>
        val updateQuery = Carts.table.filter(_.id === id).update(cart)
        db.run(updateQuery)
      case None =>
        Future.failed(new IllegalArgumentException("Please specify the cart id for update"))
    }
  }

  // Delete cart
  def deleteCart(id: Long): Future[Int] = {
    val deleteQuery = Carts.table.filter(_.id === id).delete
    db.run(deleteQuery)
  }

}

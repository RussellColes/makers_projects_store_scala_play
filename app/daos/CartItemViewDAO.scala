package daos

import models.{Cart, CartItemView, CartItems, Carts, Items}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartItemViewDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  val carts = Carts.table

  def findCartItemViews(cartId: Long): Future[Seq[CartItemView]] = {
    // joining CartItems and Items by itemId
    val query = for {
      cartItem <- CartItems.table if cartItem.cartId === cartId
      item <- Items.table if cartItem.itemId === item.id
    } yield (cartItem, item)

    db.run(query.result).map { results =>
      results.map { case (cartItem, item) =>
        CartItemView(cartItem.id.get, cartItem.cartId,item.name, cartItem.quantity, item.price)
      }
    }
  }
}
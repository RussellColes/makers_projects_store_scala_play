package models

import play.api.libs.json._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

case class CartItem(id:Option[Long], cartId:Option[Long], productId:Option[Long], quantity:Int)

object CartItem {
  implicit val cartItemFormat:OFormat[CartItem] = Json.format[CartItem]
}

class CartItems (tag: Tag) extends Table[CartItem](tag, "cart_items") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def cartId: Rep[Long] = column[Long]("cart_id")
  def productId: Rep[Long] = column[Long]("product_id")
  def quantity: Rep[Int] = column[Int]("quantity")

  def cart = foreignKey("fk_cart", cartId, Carts.table)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  def product = foreignKey("fk_product", productId, Products.table)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  override def * : ProvenShape[CartItem] =
    (id.?, cartId.?, productId.?, quantity)<>((CartItem.apply _).tupled,CartItem.unapply)
}

object CartItems {
  val table = TableQuery[CartItems]
}
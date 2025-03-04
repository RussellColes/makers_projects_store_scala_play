package models

import play.api.libs.json._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

case class CartItem(id:Option[Long], cartId:Option[Long], itemId:Option[Long], quantity:Int)

object CartItem {
  implicit val cartItemFormat:OFormat[CartItem] = Json.format[CartItem]
}

class CartItems (tag: Tag) extends Table[CartItem](tag, "cart_items") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def cartId: Rep[Long] = column[Long]("cart_id")
  def itemId: Rep[Long] = column[Long]("item_id")
  def quantity: Rep[Int] = column[Int]("quantity")

  def cart = foreignKey("fk_carts", cartId, Carts.table)(_.id, onDelete = ForeignKeyAction.Cascade)
  def item = foreignKey("fk_items", itemId, Items.table)(_.id, onDelete = ForeignKeyAction.Cascade)

  override def * : ProvenShape[CartItem] =
    (id.?, cartId.?, itemId.?, quantity)<>((CartItem.apply _).tupled,CartItem.unapply)
}

object CartItems {
  val table = TableQuery[CartItems]
}
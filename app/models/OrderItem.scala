package models

import play.api.libs.json._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

case class OrderItem(id:Option[Long], orderId:Option[Long], itemId:Option[Long], quantity:Int, unitPrice: Float)

object OrderItem {
  implicit val orderItemFormat:OFormat[OrderItem] = Json.format[OrderItem]
}

class OrderItems (tag: Tag) extends Table[OrderItem](tag, "order_items") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def orderId: Rep[Long] = column[Long]("order_id")
  def itemId: Rep[Long] = column[Long]("item_id")
  def quantity: Rep[Int] = column[Int]("quantity")
  def unitPrice: Rep[Float] = column[Float]("unit_price")

  def order = foreignKey("fk_orders_order_items", orderId, Orders.table)(_.id, onDelete = ForeignKeyAction.Cascade)
  def item = foreignKey("fk_items_order_items", itemId, Items.table)(_.id, onDelete = ForeignKeyAction.Cascade)

  override def * : ProvenShape[OrderItem] =
    (id.?, orderId.?, itemId.?, quantity, unitPrice)<>((OrderItem.apply _).tupled,OrderItem.unapply)
}

object OrderItems {
  val table = TableQuery[OrderItems]
}

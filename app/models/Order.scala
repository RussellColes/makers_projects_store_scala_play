package models

import play.api.libs.json._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import java.sql.Timestamp
import java.time.format.DateTimeFormatter

case class Order(
                  id: Option[Long],
                  userId: Long,
                  orderStatus: String,
                  orderedAt: Option[Timestamp],
                  totalAmount:BigDecimal)

object Order {
  implicit val timestampFormat: Format[java.sql.Timestamp] = new Format[java.sql.Timestamp] {
    def writes(ts: Timestamp): JsValue = {
      // Convert to Instant and format as an ISO-8601 string
      val formatted = DateTimeFormatter.ISO_INSTANT.format(ts.toInstant)
      JsString(formatted)
    }
    def reads(json: JsValue): JsResult[java.sql.Timestamp] = json.validate[Long].map(new java.sql.Timestamp(_))
  }
  implicit val orderFormat: OFormat[Order] = Json.format[Order]
}

class Orders(tag: Tag) extends Table[Order](tag, "orders") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId: Rep[Long] = column[Long]("user_id")
  def orderStatus: Rep[String] = column[String]("order_status")
  def orderedAt: Rep[Timestamp] = column[Timestamp]("ordered_at")
  def totalAmount: Rep[BigDecimal] = column[BigDecimal]("total_amount")

  def user = foreignKey("fk_user_orders", userId, Users.table)(_.id, onDelete = ForeignKeyAction.Cascade)

  override def * : ProvenShape[Order] =
    (id.?, userId, orderStatus, orderedAt.?, totalAmount) <> ((Order.apply _).tupled, Order.unapply)
}

object Orders {
  val table = TableQuery[Orders]
}

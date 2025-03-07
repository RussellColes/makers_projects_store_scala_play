package models

import play.api.libs.json._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}
import java.sql.Timestamp
import java.time.format.DateTimeFormatter


// Cart case class
case class Cart(id: Option[Long], userId: Option[Long], cartStatus: String, createdAt: Option[Timestamp], updatedAt: Option[Timestamp])

object Cart {
  implicit val timestampFormat: Format[java.sql.Timestamp] = new Format[java.sql.Timestamp] {
    def writes(ts: Timestamp): JsValue = {
      // Convert to Instant and format as an ISO-8601 string
      val formatted = DateTimeFormatter.ISO_INSTANT.format(ts.toInstant)
      JsString(formatted)
    }
    def reads(json: JsValue): JsResult[java.sql.Timestamp] = json.validate[Long].map(new java.sql.Timestamp(_))
  }
  implicit val cartFormat: OFormat[Cart] = Json.format[Cart]
}

class Carts(tag: Tag) extends Table[Cart](tag, "carts") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId: Rep[Long] = column[Long]("user_id")
  def cartStatus: Rep[String] = column[String]("cart_status")
  def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")
  def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

  def user = foreignKey("fk_user", userId, Users.table)(_.id, onDelete = ForeignKeyAction.Cascade)

  override def * : ProvenShape[Cart] =
    (id.?, userId.?, cartStatus, createdAt.?, updatedAt.?) <> ((Cart.apply _).tupled, Cart.unapply)
}

object Carts {
  val table = TableQuery[Carts]
}
package models

import play.api.libs.json._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}
import java.sql.Timestamp


// Cart case class
case class Cart(id: Option[Long], userId: Option[Long], createdAt: Timestamp, updatedAt: Timestamp)

object Cart {
  implicit val cartFormat: OFormat[Cart] = Json.format[Cart]
}

class Carts(tag: Tag) extends Table[Cart](tag, "carts") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId: Rep[Long] = column[Long]("user_id")
  def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")
  def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

  def user = foreignKey("fk_user", userId, Users.table)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  override def * : ProvenShape[Cart] =
    (id.?, userId.?, createdAt, updatedAt) <> ((Cart.apply _).tupled, Cart.unapply)
}

object Carts {
  val table = TableQuery[Carts]
}
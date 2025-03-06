//package models
//
//import play.api.libs.json._
//import slick.jdbc.PostgresProfile.api._
//import slick.lifted.{ProvenShape, Tag}
//import slick.memory.MemoryProfile.MappedColumnType
//
//import java.time.ZonedDateTime
//import java.time.ZoneOffset
//
//
//// Payments: Create a Payment model with
//// properties such as
//// amount,
//// currency,
//// status,
//// user_id,
//// order_id
//// created_at
//// completed_at
//
//// Payment case class
//case class Payment(
//                    id: Option[Long],
//                    amount: BigDecimal,
//                    currency: String,
//                    status: String,
//                    user_id: Long,
//                    order_id: Long,
//                    created_at: ZonedDateTime,
//                    completed_at: Option[ZonedDateTime],
//                  )
//
//// Companion object for payment
//object Payment {
//  implicit val paymentFormat: OFormat[Payment] = Json.format[Payment]
//}
//
//// Payment table definition
//class Payments(tag: Tag) extends Table[Payment](tag, "payments") {
//  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
//  def amount: Rep[BigDecimal] = column[BigDecimal]("amount")
//  def currency: Rep[String] = column[String]("currency")
//  def status: Rep[String] = column[String]("status")
//  def userId: Rep[Long] = column[Long]("user_id")
//  def orderId: Rep[Long] = column[Long]("order_id", O.Default(1))
//  def createdAt: Rep[ZonedDateTime] = column[ZonedDateTime]("created_at", O.Default(ZonedDateTime.now(ZoneOffset.UTC)))
//  def completedAt: Rep[Option[ZonedDateTime]] = column[Option[ZonedDateTime]]("completed_at", O.Default(None))
//
//  // Foreign key to Users table
//  def userFk = foreignKey(
//    "user_fk", userId,
//    TableQuery[Users]
//  )(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
//
////  // Foreign key to Orders table
////  def orderFk = foreignKey(
////    "order_fk", orderId,
////    TableQuery[Orders]
////  )(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
//
//  def * : ProvenShape[Payment] = (id.?, amount, currency, status, userId, orderId, createdAt, completedAt) <> ((Payment.apply _).tupled, Payment.unapply)
//}
//
//// Companion object for Payments table
//object Payments {
//  val table = TableQuery[Payments]
//}

package models

import play.api.libs.json._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import java.sql.Timestamp
import java.time.format.DateTimeFormatter

// Payment case class
case class Payment(
                    id: Option[Long],
                    amount: BigDecimal,
                    currency: String,
                    status: String,
                    userId: Long,
                    orderId: Long,
                    createdAt: Timestamp,
                    completedAt: Option[Timestamp],
                  )

// Companion object for payment
object Payment {

  implicit val timestampFormat: Format[java.sql.Timestamp] = new Format[java.sql.Timestamp] {
    def writes(ts: Timestamp): JsValue = {
      // Convert to Instant and format as an ISO-8601 string
      val formatted = DateTimeFormatter.ISO_INSTANT.format(ts.toInstant)
      JsString(formatted)
    }
    def reads(json: JsValue): JsResult[java.sql.Timestamp] = json.validate[Long].map(new java.sql.Timestamp(_))
  }

  implicit val paymentFormat: OFormat[Payment] = Json.format[Payment]

}

// Payment table definition
class Payments(tag: Tag) extends Table[Payment](tag, "payments") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def amount: Rep[BigDecimal] = column[BigDecimal]("amount")
  def currency: Rep[String] = column[String]("currency")
  def status: Rep[String] = column[String]("status")
  def userId: Rep[Long] = column[Long]("user_id")
  def orderId: Rep[Long] = column[Long]("order_id", O.Default(1))
  def createdAt: Rep[Timestamp] = column[Timestamp]("created_at", O.Default(new Timestamp(System.currentTimeMillis())))
  def completedAt: Rep[Option[Timestamp]] = column[Option[Timestamp]]("completed_at", O.Default(None))

  // Foreign key to Users table
  def userFk = foreignKey(
    "user_fk", userId,
    TableQuery[Users]
    )(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  //  // Foreign key to Orders table
  //  def orderFk = foreignKey(
  //    "order_fk", orderId,
  //    TableQuery[Orders]
  //  )(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * : ProvenShape[Payment] = (id.?, amount, currency, status, userId, orderId, createdAt, completedAt) <> ((Payment.apply _).tupled, Payment.unapply)
}

// Companion object for Payments table
object Payments {
  val table = TableQuery[Payments]
}


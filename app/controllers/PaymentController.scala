package controllers

import javax.inject._
import play.api.mvc._
import daos.PaymentDAO
import models.Payment

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import org.mindrot.jbcrypt.BCrypt
import play.api.mvc.Results._

import java.time.Instant
import java.sql.Timestamp
import scala.util.matching.Regex

@Singleton
class PaymentController @Inject()(cc: ControllerComponents, paymentDAO: PaymentDAO)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def createPayment(amount: BigDecimal, currency: String, status: String, userId: Long, orderId: Long): Future[Result] = {
    val payment = Payment(
      id = None, // Auto-generated in DB
      amount = amount,
      currency = currency,
      status = status,
      userId = userId,
      orderId = orderId,
      createdAt = Timestamp.from(Instant.now()), // ✅ Use current timestamp
      completedAt = None // ✅ Default to None
    )
    paymentDAO.createPayment(payment).map { id =>
      println(s"Payment Controller Id received: $id")
      Created(Json.obj("status" -> "success", "message" -> s"payment created: $id", "id" -> Json.toJson(id)))
    }.recover {
      case _ => InternalServerError(Json.obj("status" -> "error", "message" -> "payment could not be created"))
    }
  }

  def newPayment = Action.async(parse.json) {
    implicit request =>
      val json = request.body.as[JsObject]
      val amount = (json \ "amount").as[BigDecimal]
      val currency = (json \ "currency").as[String]
      val status = "pending"
      val userId = request.session.get("userId").map(_.toLong).getOrElse(0L)
      val orderId = (json \ "orderId").as[Long]

      createPayment(amount, currency, status, userId, orderId)
  }

  def findPaymentById(id: Long): Future[Result] = {
    val payment = paymentDAO.findPaymentById(id)
    payment.map {
      case Some(payment2) =>
//        println(payment2.amount)
        Ok(Json.obj("status" -> "success", "message" -> "payment found", "payment" -> Json.toJson(payment2)))
      case _ => BadRequest(Json.obj("status" -> "error", "message" -> "payment not found" ))
    }
  }

  def updatePaymentStatus(id: Long): Action[AnyContent] = Action.async {
    paymentDAO.updatePaymentStatus(id).map {
      case 0 => NotFound(Json.obj("message" -> s"Payment with ID $id not found"))
      case _ => Ok(Json.obj("message" -> s"Payment with ID $id updated to 'completed'"))
    }.recover {
      case _ => InternalServerError(Json.obj("message" -> "An error occurred while updating payment status"))
    }
  }

//  def updatePaymentStatus1(id: Long): Future[Result] = Action.async(parse.json) {
//    implicit request => request.body.validate[Payment].fold(
//      errors => Future.successful(BadRequest(Json.obj("message" -> "Invalid JSON"))),
//      updatedPayment => {
//        paymentDAO.findPaymentById(id).flatMap {
//          case Some(-) =>
//            paymentDAO.updatePaymentStatus(id, updatedPayment).map { rowsUpdated =>
//              if (rowsUpdated > 0) Ok(Json.obj("message" -> "Payment updated successfully"))
//              else InternalServerError(Json.obj("message" -> "Failed to update payment"))
//            }
//          case None =>
//            Future.successful(NotFound(Json.obj("message" -> s"Payment with id $id not found")))
//        }
//      }
//    )
//
//    Future.successful(Results.NotImplemented("This method is not yet implemented."))
//  }

}




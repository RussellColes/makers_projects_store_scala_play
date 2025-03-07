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

  def getPayment(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    paymentDAO.findPaymentById(id).map {
      case Some(payment) => Ok(views.html.payment(payment))
      case None => NotFound("Payment not found")
    }
  }
}




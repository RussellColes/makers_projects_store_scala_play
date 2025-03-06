package controllers

import daos.{DbDAO, OrderDAO, PaymentDAO, UserDAO}
import models.{Order, User}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.Helpers._
import play.api.test._
import slick.jdbc.JdbcProfile
import slick.lifted
import slick.lifted.TableQuery
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class PaymentControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    // Clear tables
    val dbDAO = app.injector.instanceOf[DbDAO]
    Await.result(dbDAO.truncateAllTables(), 100.seconds)

    //Add test user
    val userDAO = inject[UserDAO]
    val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])
    val request = FakeRequest(POST, "/signUp")
      .withJsonBody(Json.obj(
        "username" -> "testuser",
        "email" -> "test@example.com",
        "password" -> "password123!")
      )
      .withCSRFToken
    Await.result(call(userController.signUp, request), 100.seconds)

    //    def createOrder(order: Order)
    val orderDAO = inject[OrderDAO]
    val order = Order(
      id = None,
      userId = 1,
      orderStatus = "pending",
      orderedAt = None,
      totalAmount =  1000.00
    )
    Await.result(orderDAO.createOrder(order), 10.seconds)
    super.beforeEach()
  }
  "PaymentController POST /createPayment" should {

    "create a new payment" in {
      val paymentDAO = inject[PaymentDAO]
      val paymentController = new PaymentController(stubControllerComponents(), paymentDAO)(inject[ExecutionContext])
      val result = paymentController.createPayment(

        amount = 29.99,
        currency = "USD",
        status = "pending",
        userId = 1,
        orderId = 1,
      )
      status(result) mustBe CREATED
      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "status").as[String] mustBe "success"
      (jsonResponse \ "message").as[String] must include("payment created")

    }

    "create a new payment using a route" in {
      val paymentDAO = inject[PaymentDAO]
      val paymentController = new PaymentController(
        stubControllerComponents(), paymentDAO)(inject[ExecutionContext]
      )

      val request = FakeRequest(POST, "/payment")
        .withSession("userId" -> "1", "username" -> "testuser")
        .withJsonBody(Json.obj(
          "amount" -> 29.99,
          "currency" -> "USD",
          "orderId" -> 1
        ))
        .withCSRFToken
      val result = call(paymentController.newPayment, request)
      status(result) mustBe CREATED
      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "status").as[String] mustBe "success"
      (jsonResponse \ "message").as[String] must include("payment created")
    }
  }
  "PaymentController GET /payment" should {

    "find payment in database after it has been created" in {
      val paymentDAO = inject[PaymentDAO]
      val paymentController = new PaymentController(stubControllerComponents(), paymentDAO)(inject[ExecutionContext])
      Await.result(paymentController.createPayment(

        amount = 29.99,
        currency = "USD",
        status = "pending",
        userId = 1,
        orderId = 1,
      ), 10.seconds)

      val result = paymentController.findPaymentById(1)
      status(result) mustBe OK

      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "status").as[String] mustBe "success"
      (jsonResponse \ "message").as[String] must include("payment found")
      println(jsonResponse \ "payment" \ "amount")
      (jsonResponse \ "payment" \ "id").as[Long] mustBe (1)
      (jsonResponse \ "payment" \ "amount").as[BigDecimal] mustBe (29.99)


    }
  }
}

//      val request = FakeRequest(POST, "/payment")
//        .withJsonBody(Json.obj(
//          "amount" -> 29.99,
//          "currency" -> "USD",
//          "status" -> "pending",
//          "user_id" -> 1,
//          "order_id" -> 1,
//
//        )
//        )
//        .withCSRFToken



// Verify user is actually created in the database
//      val maybeUser = await(userDAO.findUserByUsername("testuser"))
//      maybeUser must not be empty
//      maybeUser.get.email mustBe "test@example.com"
//    "return bad request for taken username" in {
//      val userDAO = inject[UserDAO]
//      val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])
//
//      val request = FakeRequest(POST, "/signUp")
//        .withJsonBody(Json.obj(
//          "username" -> "Alan",
//          "email" -> "alan@gmail.com",
//          "password" -> "Password123!")
//        )
//        .withCSRFToken
//
//      await(call(userController.signUp, request))
//
//      val requestDuplicate = FakeRequest(POST, "/signUp")
//        .withJsonBody(Json.obj(
//          "username" -> "Alan",
//          "email" -> "alan2@gmail.com",
//          "password" -> "Password123!2")
//        )
//        .withCSRFToken
//
//      val resultDuplicate = call(userController.signUp, requestDuplicate)
//
//      status(resultDuplicate) mustBe BAD_REQUEST
//      val jsonBody = contentAsJson(resultDuplicate)
//      jsonBody("message").as[String] mustBe "Username already exists"
//    }
//    "return bad request for invalid email address format" in {
//      val userDAO = inject[UserDAO]
//      val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])
//
//      val request = FakeRequest(POST, "/signUp")
//        .withJsonBody(Json.obj(
//          "username" -> "Alan",
//          "email" -> "not-an-email",
//          "password" -> "Password123!2")
//        )
//        .withCSRFToken
//
//      val result = call(userController.signUp, request)
//
//      status(result) mustBe BAD_REQUEST
//      val jsonBody = contentAsJson(result)
//      jsonBody("message").as[String] mustBe ("Email address is not in a valid format")
//    }
//    "return bad request for invalid password" in {
//      val userDAO = inject[UserDAO]
//      val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])
//
//      val request = FakeRequest(POST, "/signUp")
//        .withJsonBody(Json.obj(
//          "username" -> "alan",
//          "email" -> "alan2@gmail.com",
//          "password" -> "hfurh")
//        )
//        .withCSRFToken
//
//      val result = call(userController.signUp, request)
//
//      status(result) mustBe BAD_REQUEST
//      val jsonBody = contentAsJson(result)
//      jsonBody("message").as[String] mustBe ("Password must contain more than 8 characters and a special character")
//    }
//  }
//  "UserController POST /logIn" should {
//    "log in and put username in session" in {
//      val userDAO = inject[UserDAO]
//      val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])
//
//      val request = FakeRequest(POST, "/signUp")
//        .withJsonBody(Json.obj(
//          "username" -> "alan",
//          "email" -> "alan2@gmail.com",
//          "password" -> "password$")
//        )
//        .withCSRFToken
//
//      await(call(userController.signUp, request))
//
//      val requestLogin = FakeRequest(POST, "/logIn")
//        .withJsonBody(Json.obj(
//          "username" -> "alan",
//          "password" -> "password$")
//        )
//        .withCSRFToken
//
//      val result = call(userController.logIn, requestLogin)
//      status(result) mustBe OK
//      val jsonBody = contentAsJson(result)
//      //      jsonBody("message").as[String] mustBe ("Password must contain more than 8 characters and a special character")
//      println(jsonBody)
//
//      session(result).get("username") mustBe Some("alan")
//      session(result).get("userId") mustBe Some("1")
//    }
//

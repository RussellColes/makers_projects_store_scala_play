package controllers

import daos.UserDAO
import daos.DbDAO
import models.Users
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
import scala.concurrent.Await
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext

class UserControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    // Get your DAO from the injector
    val dbDAO = app.injector.instanceOf[DbDAO]
    // Clear all tables before each test
    Await.result(dbDAO.truncateAllTables(), 100.seconds)

    super.beforeEach()
  }
  "UserController POST /signUp" should {

    "create a new user" in {
      val userDAO = inject[UserDAO]
      val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])

      val request = FakeRequest(POST, "/signUp")
        .withJsonBody(Json.obj(
          "username" -> "testuser",
          "email" -> "test@example.com",
          "password" -> "password123!")
        )
        .withCSRFToken

      val result = call(userController.signUp, request)

      status(result) mustBe CREATED
      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "status").as[String] mustBe "success"
      (jsonResponse \ "message").as[String] must include("User")

      // Verify user is actually created in the database
      val maybeUser = await(userDAO.findUserByUsername("testuser"))
      maybeUser must not be empty
      maybeUser.get.email mustBe "test@example.com"
    }

    "return bad request for taken username" in {
      val userDAO = inject[UserDAO]
      val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])

      val request = FakeRequest(POST, "/signUp")
        .withJsonBody(Json.obj(
          "username" -> "Alan",
          "email" -> "alan@gmail.com",
          "password" -> "Password123!")
        )
        .withCSRFToken

      await(call(userController.signUp, request))

      val requestDuplicate = FakeRequest(POST, "/signUp")
        .withJsonBody(Json.obj(
          "username" -> "Alan",
          "email" -> "alan2@gmail.com",
          "password" -> "Password123!2")
        )
        .withCSRFToken

      val resultDuplicate = call(userController.signUp, requestDuplicate)

      status(resultDuplicate) mustBe BAD_REQUEST
      val jsonBody = contentAsJson(resultDuplicate)
      jsonBody("message").as[String] mustBe "Username already exists"
    }
    "return bad request for invalid email address format" in {
      val userDAO = inject[UserDAO]
      val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])

      val request = FakeRequest(POST, "/signUp")
        .withJsonBody(Json.obj(
          "username" -> "Alan",
          "email" -> "not-an-email",
          "password" -> "Password123!2")
        )
        .withCSRFToken

      val result = call(userController.signUp, request)

      status(result) mustBe BAD_REQUEST
      val jsonBody = contentAsJson(result)
      jsonBody("message").as[String] mustBe ("Email address is not in a valid format")
    }
    "return bad request for invalid password" in {
      val userDAO = inject[UserDAO]
      val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])

      val request = FakeRequest(POST, "/signUp")
        .withJsonBody(Json.obj(
          "username" -> "alan",
          "email" -> "alan2@gmail.com",
          "password" -> "hfurh")
        )
        .withCSRFToken

      val result = call(userController.signUp, request)

      status(result) mustBe BAD_REQUEST
      val jsonBody = contentAsJson(result)
      jsonBody("message").as[String] mustBe ("Password must contain more than 8 characters and a special character")
    }
  }
  "UserController POST /logIn" should {
    "log in and put username in session" in {
      val userDAO = inject[UserDAO]
      val userController = new UserController(stubControllerComponents(), userDAO)(inject[ExecutionContext])

      val request = FakeRequest(POST, "/signUp")
        .withJsonBody(Json.obj(
          "username" -> "alan",
          "email" -> "alan2@gmail.com",
          "password" -> "password$")
        )
        .withCSRFToken

      await(call(userController.signUp, request))

      val requestLogin = FakeRequest(POST, "/logIn")
        .withJsonBody(Json.obj(
          "username" -> "alan",
          "password" -> "password$")
        )
        .withCSRFToken

      val result = call(userController.logIn, requestLogin)
      status(result) mustBe OK
      val jsonBody = contentAsJson(result)
//      jsonBody("message").as[String] mustBe ("Password must contain more than 8 characters and a special character")
      println(jsonBody)

      session(result).get("username") mustBe Some("alan")
      session(result).get("userId") mustBe Some("1")
    }

  }
}

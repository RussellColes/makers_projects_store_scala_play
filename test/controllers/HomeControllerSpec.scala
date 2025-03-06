package controllers

import daos.UserDAO
import daos.ItemDAO
import models.Users
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.Play.materializer
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Session

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

    val authenticatedRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/")
      .withSession("userId" -> "123", "username" -> "alan")

    val itemDAO: ItemDAO = inject[ItemDAO]


  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      val controller = new HomeController(stubControllerComponents(), itemDAO)(inject[ExecutionContext])
      val home = controller.index().apply(authenticatedRequest)

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to The Makers Store")
    }

    "render the index page from the application" in {
      val controller = inject[HomeController]
      val home = controller.index().apply(authenticatedRequest)

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to The Makers Store")
    }

    "render the index page from the router" in {
      val request = authenticatedRequest
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to The Makers Store")
    }
  }
}

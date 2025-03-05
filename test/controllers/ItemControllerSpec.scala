package controllers

import daos.ItemDAO
import models.{Item, Items}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.CSRFRequest
import org.scalatest.BeforeAndAfterEach
import play.api.Play.materializer

import scala.concurrent.{Await, ExecutionContext, Future}


class ItemControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  val itemDAO: ItemDAO = inject[ItemDAO]
  val itemController = new ItemController(stubControllerComponents(), itemDAO)(inject[ExecutionContext])

  // These aren't necessary until we
  val testItem1 = Item(None,"Test Item 1", 10.99, "Description 1", "London", "UK")
  val testItem2 = Item(None, "Test Item 2", 20.50, "Description 2", "London", "UK")
  val testItem3 = Item(None, "Test Item 3", 5.25,"Description 3", "London", "UK")

  override def beforeEach(): Unit = {
    // Clear the items table before each test and populate with test data
//    val clearResult = Await.result(itemDAO.clearItems(), 5.seconds)

    // Insert test items
//    val insertResults = Await.result(
//      Future.sequence(Seq(
//        itemDAO.createItem(testItem1),
//        itemDAO.createItem(testItem2),
//        itemDAO.createItem(testItem3)
//      )),
//      5.seconds
//    )

    super.beforeEach()
  }
  "ItemController POST /createItem" should {
    "create a new item" in {
      val itemDAO = inject[ItemDAO]
      val itemController = new ItemController(stubControllerComponents(), itemDAO)(inject[ExecutionContext])

      val request = FakeRequest(POST, "/createItem")
        .withJsonBody(Json.obj(
//          "id" -> "123L",
          "name" -> "Test Item 1",
          "price" -> 10.99,
          "description" -> "Description 1",
          "location" -> "London",
          "country" -> "UK"
        ))
        .withCSRFToken

      val result = call(itemController.createItem, request)


      status(result) mustBe CREATED
      val jsonResponse = contentAsJson(result)

      println(jsonResponse)
      (jsonResponse \ "status").as[String] mustBe "success"
      (jsonResponse \ "message").as[String] must include("Item")

      // Verify the item was created in the database
      // You can do this by getting the ID from the response or by searching for the name
      // If your response includes the item ID:
      val itemId = (jsonResponse \ "id").as[Long]
      val maybeItem = await(itemDAO.findItemById(itemId))
      maybeItem must not be empty
//      below can be used for another test if we want to verify the details
//      maybeItem.get.name mustBe "Test Item 1"
//      maybeItem.get.price mustBe 10.99
//      maybeItem.get.description mustBe "Description 1"
    }
  }
//  "ItemControllerGet" should {
//    "return all items when calling getAllItems" in {
//      // Call the method under test
//      val items = Await.result(itemDAO.getAllItems(), 5.seconds)
//
//      // Verify the results
//      items.length mustBe 3
//      items must contain(testItem1)
//      items must contain(testItem2)
//      items must contain(testItem3)
//    }
//
//    "find an item by id when the item exists" in {
//      // Call the method under test
//      val maybeItem = Await.result(itemDAO.findItemById(2L), 5.seconds)
//
//      // Verify the result
//      maybeItem mustBe defined
//      maybeItem.get mustBe testItem2
//    }
//
//    "return None when finding an item by id that doesn't exist" in {
//      // Call the method under test
//      val maybeItem = Await.result(itemDAO.findItemById(99L), 5.seconds)
//
//      // Verify the result
//      maybeItem mustBe None
//    }
//  }
}

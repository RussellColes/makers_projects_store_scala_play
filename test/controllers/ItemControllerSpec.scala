package controllers

import daos.ItemDAO
import daos.DbDAO
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
  implicit val ec: ExecutionContext = inject[ExecutionContext]
  val itemDAO: ItemDAO = inject[ItemDAO]
  val dbDAO: DbDAO = inject[DbDAO]
  val itemController = new ItemController(stubControllerComponents(), itemDAO)(inject[ExecutionContext])

  // These aren't necessary until we
  val testItem1: Item = Item(None,"Test Item 1", 10.99, "Description 1", "London", "UK")
  val testItem2: Item = Item(None, "Test Item 2", 20.50, "Description 2", "London", "UK")
  val testItem3: Item = Item(None, "Test Item 3", 5.25,"Description 3", "London", "UK")



  override def beforeEach(): Unit = {
//    Clear all tables before running tests
    val clearTables = Await.result(dbDAO.truncateAllTables(), 10.seconds)

//     Insert test items
    val insertResults = Await.result(
      Future.sequence(Seq(
        itemDAO.createItem(testItem1),
        itemDAO.createItem(testItem2),
        itemDAO.createItem(testItem3)
      )),
      10.seconds
    )

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


      val itemId = (jsonResponse \ "id").as[Long]
      val maybeItem = await(itemDAO.findItemById(itemId))
      maybeItem must not be empty

    }
  }
  "ItemControllerGet" should {
    "return all items when calling getAllItems" in {
      // Call the method under test
      val items = Await.result(itemDAO.getAllItems(), 5.seconds)

      // Verify the results
      items.length mustBe 3
      items.map(_.name) must contain allOf(
        testItem1.name,
        testItem2.name,
        testItem3.name
      )
    }

    "find an item by id when the item exists" in {
      // Call the method under test
      val allItems = Await.result(itemDAO.getAllItems(), 5.seconds)
      val testItem = allItems.find(_.name == testItem3.name).getOrElse(
        fail("Test item not found in database")
      )
      val itemId = testItem.id.getOrElse(fail("Item has no ID"))

      val maybeItem = Await.result(itemDAO.findItemById(itemId), 5.seconds)
      println(f"testItem3 name ${testItem3.name} maybeItemName: ${maybeItem.get.name}")

      maybeItem mustBe defined
      maybeItem.get.name mustBe testItem3.name
    }

    "return None when finding an item by id that doesn't exist" in {

      val maybeItem = Await.result(itemDAO.findItemById(99L), 5.seconds)

      maybeItem mustBe None
    }
  }
  "ItemController Post/DeleteItem" should {
    "delete an existing item and return NoContent" in {

      val beforeItems = Await.result(itemDAO.getAllItems(), 5.seconds)
      val beforeCount = beforeItems.length
      val itemToDelete = beforeItems.head
      val itemId = itemToDelete.id.get


      val request = FakeRequest(DELETE, s"/deleteItem/$itemId")
        .withCSRFToken
      val result = call(itemController.deleteItem(itemId), request)


      status(result) mustBe NO_CONTENT


      val afterItems = Await.result(itemDAO.getAllItems(), 5.seconds)
      afterItems.length mustBe (beforeCount - 1)


      val itemStillExists = Await.result(itemDAO.findItemById(itemId), 5.seconds)
      itemStillExists must be(None)


      afterItems.map(_.name) must not contain itemToDelete.name
    }

    "return NotFound when deleting a non-existent item" in {

      val nonExistentId = 9999L

      val request = FakeRequest(DELETE, s"/deleteItem/$nonExistentId")
        .withCSRFToken
      val result = call(itemController.deleteItem(nonExistentId), request)

      status(result) mustBe NOT_FOUND
      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] must include("not found")
    }

  }
  "ItemController PUT /updateItem/:id" should {
    "Update an existing item" in {
      val itemToUpdate = Item(None,"Updated Item 3",15.99,"Updated item 1","Paris","France")
      val maybeItem = Await.result(itemDAO.updateItem(3, itemToUpdate), 5.seconds)

      val request = FakeRequest(PUT, s"/updateItem/3")
        .withJsonBody(Json.toJson(itemToUpdate))
        .withCSRFToken

      val result = call(itemController.updateItem(3), request)

      status(result) mustBe OK
      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "message").as[String] must include("updated successfully")

      val updatedItemFromDb = Await.result(itemDAO.findItemById(3), 5.seconds)
      updatedItemFromDb must be(defined)
      updatedItemFromDb.get.name must include("Updated")
      updatedItemFromDb.get.price mustBe (15.99)
      updatedItemFromDb.get.description must include("Updated")

    }
  }
}

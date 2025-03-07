import java.sql.Timestamp
import java.time.Instant
import controllers.{CartController, routes}
import models.{Cart, CartItem, CartItemView}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.test.CSRFTokenHelper.CSRFRequest

import scala.concurrent.Future

class CartControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with MockitoSugar {

  val mockCartDAO = mock[daos.CartDAO]
  val mockCartItemDAO = mock[daos.CartItemDAO]
  val mockCartItemViewDAO = mock[daos.CartItemViewDAO]

  val cc = inject[ControllerComponents]

  val controller = new CartController(cc, mockCartDAO, mockCartItemDAO, mockCartItemViewDAO)

  "CartController" should {

    "add item to cart when active cart exists" in {
      val userId = 1L
      val activeCart = Cart(Some(1), Some(userId), "active", Some(Timestamp.from(Instant.now())), Some(Timestamp.from(Instant.now())))
      when(mockCartDAO.findActiveCart(userId)).thenReturn(Future.successful(Some(activeCart)))
      val createdCartItem = CartItem(Some(1), activeCart.id, Some(2), 1)
      when(mockCartItemDAO.createCartItem(any[CartItem])).thenReturn(Future.successful(createdCartItem))

      val request = FakeRequest(POST, "/cart/add/2").withSession("userId" -> "1")
      val result = controller.addItemToCart(2L).apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.CartController.myCart().url)
    }

    "add item to cart when no active cart exists" in {
      val userId = 1L
      when(mockCartDAO.findActiveCart(userId)).thenReturn(Future.successful(None))
      val newCart = Cart(Some(2), Some(userId), "active", Some(Timestamp.from(Instant.now())), Some(Timestamp.from(Instant.now())))
      when(mockCartDAO.createCart(any[Cart])).thenReturn(Future.successful(newCart))
      val createdCartItem = CartItem(Some(1), newCart.id, Some(2), 1)
      when(mockCartItemDAO.createCartItem(any[CartItem])).thenReturn(Future.successful(createdCartItem))

      val request = FakeRequest(POST, "/cart/add/2").withSession("userId" -> "1")
      val result = controller.addItemToCart(2L).apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.CartController.myCart().url)
    }

    "redirect to login if userId not in session for addItemToCart" in {
      val request = FakeRequest(POST, "/cart/add/2")
      val result = controller.addItemToCart(2L).apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.UserController.logIn().url)
    }

    "check out cart successfully" in {
      val userId = 1L
      val now = Timestamp.from(Instant.now())
      val activeCart = Cart(Some(1), Some(userId), "active", Some(now), Some(now))
      when(mockCartDAO.findActiveCart(userId)).thenReturn(Future.successful(Some(activeCart)))
      when(mockCartDAO.updateCart(any[Cart])).thenReturn(Future.successful(1))

      val request = FakeRequest(POST, "/cart/checkout").withSession("userId" -> "1")
      val result = controller.cartCheckOut.apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.HomeController.index().url)
    }

    "return BadRequest for checkout if active cart not found" in {
      val userId = 1L
      when(mockCartDAO.findActiveCart(userId)).thenReturn(Future.successful(None))
      val request = FakeRequest(POST, "/cart/checkout").withSession("userId" -> "1")
      val result = controller.cartCheckOut.apply(request)

      status(result) mustBe BAD_REQUEST
    }


    "render myCart view with cart items" in {
      val userId = 1L
      val now = Timestamp.from(Instant.now())
      val activeCart = Cart(Some(1), Some(userId), "active", Some(now), Some(now))
      when(mockCartDAO.findActiveCart(userId)).thenReturn(Future.successful(Some(activeCart)))
      val cartItemViews = Seq(
        CartItemView(1, Some(1L), "Product A", 1, 10.0),
        CartItemView(2, Some(1L),"Product B", 2, 20.0)
      )
      when(mockCartItemViewDAO.findCartItemViews(1)).thenReturn(Future.successful(cartItemViews))

      val request = FakeRequest(GET, "/cart").withSession("userId" -> "1").withCSRFToken
      val result = controller.myCart().apply(request)

      status(result) mustBe OK
      contentAsString(result) must include ("Product A")
      contentAsString(result) must include ("Product B")
    }

    "updateCartItem updates quantity and redirects to myCart" in {
      val existingCartItem = CartItem(Some(1L),Some(1L),Some(1L),10)
      when(mockCartItemDAO.findById(1L)).thenReturn(Future.successful(Some(existingCartItem)))
      when(mockCartItemDAO.updateCartItem(any[CartItem])).thenReturn(Future.successful(1))
      val request = FakeRequest(POST, "/cart/items/update")
        .withFormUrlEncodedBody("id" -> "1", "quantity" -> "20")
        .withCSRFToken
      val result = controller.updateCartItem().apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.CartController.myCart().url)
    }

    "deleteCartItem deletes the item and redirect to myCart when valid form data is provided" in {
      when(mockCartItemDAO.deleteCartItem(1L)).thenReturn(Future.successful(1))
      val request = FakeRequest(POST, "/carts/items/delete")
        .withFormUrlEncodedBody("id" -> "1")
        .withCSRFToken
      val result = controller.deleteCartItem().apply(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.CartController.myCart().url)
    }
  }
}

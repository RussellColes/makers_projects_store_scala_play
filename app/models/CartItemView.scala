package models

case class CartItemView(id: Long, cartId: Option[Long], productName: String, quantity: Int, price: Double)


package daos

import models.{Order, Orders}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  val orders = Orders.table

  def createOrder(order: Order): Future[Order] = {
    val createQuery = (Orders.table returning Orders.table.map(_.id)
      into ((order, id) => order.copy(id = Some(id)))
      += order)
    db.run(createQuery)
  }

  def findById(id: Long): Future[Option[Order]] = {
    val findByIdQuery = Orders.table.filter(_.id === id).result.headOption
    db.run(findByIdQuery)
  }

  def findByUserId(userId: Long): Future[Seq[Order]] = {
    val findByUserIdQuery = Orders.table.filter(_.userId === userId).result
    db.run(findByUserIdQuery)
  }

  def updateOrder(order: Order): Future[Int] = {
    order.id match {
      case Some(id) =>
        val updateQuery = Orders.table.filter(_.id === id).update(order)
        db.run(updateQuery)
      case None =>
        Future.failed(new IllegalArgumentException("Please specify the order id for update"))
    }
  }

  def deleteOrder(id: Long): Future[Int] = {
    val deleteQuery = Orders.table.filter(_.id === id).delete
    db.run(deleteQuery)
  }

}

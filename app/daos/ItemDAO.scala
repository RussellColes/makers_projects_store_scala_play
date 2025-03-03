package daos

import models.{Item, Items}
import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItemDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  val items = Items.table

//  def addUser(item: Item): Future[Long] = {
//    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
//    val userWithHashedPassword = user.copy(password = hashedPassword)
//    db.run(users returning users.map(_.id) += userWithHashedPassword)
//  }

  def findItemByName(name: String): Future[Option[Item]] = {
    db.run(items.filter(_.name === name).result.headOption) // maybe remove headOption if we want to return everything
  }

  def findItemById(id: String): Future[Option[Item]] = {
    db.run(items.filter(_.name === name).result.headOption)
  }

  private class Items(tag: Tag) extends Table[Item](tag, "items") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def price = column[String]("price")
    def description = column[String]("description")

    def * = (id.?, name, price, description) <> ((Item.apply _).tupled, Item.unapply)
  }
}

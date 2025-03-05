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

//  https://scala-slick.org/doc/3.0.0/dbio.html?highlight=action
//  def getAllItems(): Future[Seq[Item]] = {
//    db.run(items.result)
//  }

//  def findItemByName(name: String): Future[Option[Item]] = {
//    db.run(items.filter(_.name === name).result.headOption) // maybe remove headOption if we want to return everything
//  }

  def getAllItems(): Future[Seq[Item]] = {
    db.run(items.result)
  }

  def findItemById(id: Long): Future[Option[Item]] = {
    db.run(items.filter(_.id === id).result.headOption)
  }


  private class Items(tag: Tag) extends Table[Item](tag, "items") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def price = column[Double]("price")
    def description = column[String]("description")


//    The * projection of the table used as default for queries and inserts.
//    Should include all columns as a tuple, HList or custom shape and optionally map them to a custom entity type using the <> operator.
//    The ProvenShape return type ensures that there is a Shape available for translating between the Column-based type in *
//    and the client-side type without Column in the table's type parameter.
    def * = (id.?, name, price, description) <> ((Item.apply _).tupled, Item.unapply)
  }
}


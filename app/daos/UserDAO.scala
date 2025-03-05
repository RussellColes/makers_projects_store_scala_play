package daos

import models.{User, Users}
import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
//import scala.concurrent.ExecutionContext.Implicits.global

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  val users = Users.table

  def addUser(user: User): Future[Long] = {
    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
    val userWithHashedPassword = user.copy(password = hashedPassword)
    db.run(users returning users.map(_.id) += userWithHashedPassword)
  }

  def findUserByUsername(username: String): Future[Option[User]] = {
    val query = db.run(users.filter(_.username === username).result.headOption)
    query.map { userOpt =>
      println(s"Database result: $userOpt")  // Print the actual result
      userOpt
      }

//    println(db.run(users.filter(_.username === username).result.headOption))
//    db.run(users.filter(_.username === username).result.headOption)
  }

  def clearUsers(): Future[Int] = {
    db.run(sqlu"TRUNCATE TABLE users RESTART IDENTITY")
  }

  private class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def email = column[String]("email")
    def password = column[String]("password")

    def * = (id.?, username, email, password) <> ((User.apply _).tupled, User.unapply)
  }
}

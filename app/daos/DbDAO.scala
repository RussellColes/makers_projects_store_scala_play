package daos


import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DbDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  def truncateAllTables(): Future[Int] = {
    val truncateQuery = sqlu"TRUNCATE TABLE cart_items, items, carts, users RESTART IDENTITY CASCADE"
    db.run(truncateQuery)
  }


}

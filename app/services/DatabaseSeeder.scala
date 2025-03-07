package services

import daos.{ItemDAO, UserDAO}
import javax.inject.{Inject, Singleton}
import models.{Item, User}
import play.api.{Configuration, Environment, Logger, Mode}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DatabaseSeeder @Inject()(
                                userDAO: UserDAO,
                                itemDAO: ItemDAO,
                                environment: Environment,
                                configuration: Configuration,
                                lifecycle: ApplicationLifecycle
                              )(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)
  logger.info("DATABASE SEEDER STARTING")

  // Check if seeding is enabled in configuration
  private val seedEnabled = configuration
    .getOptional[Boolean]("app.database.seed.enabled")
    .getOrElse(environment.mode == Mode.Dev)

  private val cleanBeforeSeed = configuration
    .getOptional[Boolean]("app.database.seed.cleanBeforeSeed")
    .getOrElse(false)

  // Run seeding if enabled, but ensure it completes before shutdown
  if (seedEnabled) {
    logger.info("Database seeding is enabled")
    val seedFuture = seed()

    // Register callback for application shutdown
    lifecycle.addStopHook { () =>
      logger.info("Application shutting down, ensuring seed operation completes")
      seedFuture.map(_ => ())
    }

    // Log when seeding completes
    seedFuture.foreach(_ => logger.info("Seed data loaded successfully"))
    seedFuture.failed.foreach(e => logger.error(s"Seeding failed: ${e.getMessage}", e))
  } else {
    logger.info("Database seeding is disabled")
  }

  def seed(): Future[Unit] = {
    // Run operations sequentially to avoid thread pool issues
    for {
      // Check if tables are empty
      usersEmpty <- userDAO.countUsers().map(_ == 0)
      itemsEmpty <- itemDAO.countItems().map(_ == 0)

      _ = logger.info(s"Tables empty? Users: $usersEmpty, Items: $itemsEmpty")

      // Clean tables if configured to do so
      _ <- if (cleanBeforeSeed) cleanTables() else Future.successful(())

      // Only seed if tables are empty or we just cleaned them
      _ <- if (usersEmpty || cleanBeforeSeed) seedUsersSequentially() else Future.successful(())
      _ <- if (itemsEmpty || cleanBeforeSeed) seedItemsSequentially() else Future.successful(())
    } yield ()
  }

  private def cleanTables(): Future[Unit] = {
    logger.info("Cleaning tables before seeding")
    // Clean items first, then users (in case there are dependencies)
    itemDAO.clearItems()
      .flatMap(_ => userDAO.clearUsers())
      .map(_ => ())
      .recover { case e: Exception =>
        logger.error(s"Error cleaning tables: ${e.getMessage}", e)
        throw e
      }
  }

  private def seedUsersSequentially(): Future[Unit] = {
    logger.info("Starting to seed users table")

    // Add users one by one
    userDAO.addUser(User(None, "admin", "admin@example.com", "password123!"))
      .flatMap { id =>
        logger.info(s"Added admin user with ID: $id")
        userDAO.addUser(User(None, "john", "john@example.com", "password123!"))
      }
      .flatMap { id =>
        logger.info(s"Added john user with ID: $id")
        userDAO.addUser(User(None, "alice", "alice@example.com", "password123!"))
      }
      .flatMap { id =>
        logger.info(s"Added alice user with ID: $id")
        userDAO.addUser(User(None, "bob", "bob@example.com", "password123!"))
      }
      .flatMap { id =>
        logger.info(s"Added bob user with ID: $id")
        userDAO.addUser(User(None, "demo", "demo@example.com", "password123!"))
      }
      .map { id =>
        logger.info(s"Added demo user with ID: $id")
        logger.info("Completed seeding users")
      }
      .recover { case e: Exception =>
        logger.error(s"Error seeding users: ${e.getMessage}", e)
        // Don't re-throw to avoid crashing the application
      }
  }

  private def seedItemsSequentially(): Future[Unit] = {
    logger.info("Starting to seed items table")

    // Items to add - a few examples
    val items = List(
      Item(None, "MacBook Pro", 1299.99, "Latest model with M2 chip", "San Francisco", "USA"),
      Item(None, "Samsung Galaxy S22", 799.99, "Android flagship phone", "Seoul", "South Korea"),
      Item(None, "Sony PlayStation 5", 499.99, "Gaming console", "Tokyo", "Japan"),
      Item(None, "Dyson V11 Vacuum", 599.99, "Cordless vacuum cleaner", "London", "UK"),
      Item(None, "Nespresso Coffee Maker", 199.99, "Premium coffee machine", "Zurich", "Switzerland"),
      Item(None, "Nike Air Max", 129.99, "Running shoes", "Portland", "USA"),
      Item(None, "Levi's 501 Jeans", 69.99, "Classic denim jeans", "San Francisco", "USA"),
      Item(None, "The Great Gatsby", 12.99, "Classic novel by F. Scott Fitzgerald", "New York", "USA"),
      Item(None, "Italian Leather Sofa", 1299.99, "Handcrafted genuine leather sofa", "Milan", "Italy"),
      Item(None, "German Cuckoo Clock", 349.99, "Traditional Black Forest clock", "Munich", "Germany"),
      Item(None, "Japanese Matcha Set", 79.99, "Traditional tea ceremony kit", "Kyoto", "Japan"),
      Item(None, "French Wine Collection", 199.99, "Selection of premium wines", "Bordeaux", "France")
    )

    // Add items one by one in sequence
    def addItemsRecursively(items: List[Item]): Future[Unit] = {
      items match {
        case Nil => Future.successful(())
        case item :: rest =>
          itemDAO.createItem(item).flatMap { id =>
            logger.info(s"Added item '${item.name}' with ID: $id")
            addItemsRecursively(rest)
          }
      }
    }

    addItemsRecursively(items)
      .map(_ => logger.info("Completed seeding items"))
      .recover { case e: Exception =>
        logger.error(s"Error seeding items: ${e.getMessage}", e)
        // Don't re-throw to avoid crashing the application
      }
  }
}
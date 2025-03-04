package controllers

import javax.inject._
import play.api.mvc._
import daos.UserDAO
import models.User
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import org.mindrot.jbcrypt.BCrypt
import play.api.mvc.Results._
import scala.util.matching.Regex

@Singleton
class UserController @Inject()(cc: ControllerComponents, userDAO: UserDAO)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  val userForm: Form[User] = Form(
    mapping(
      "id" -> optional(longNumber),
      "username" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  def showSignUpForm = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.signup(""))
  }

// CHECK USERNAME HELPER FUNCTIONS
  private def usernameIsFree (username: String): Future[Boolean] = {
    val findUserByUsername = userDAO.findUserByUsername(username)
    findUserByUsername.map { maybeUser =>
      println(s"found user: $maybeUser")
      maybeUser match {
        case Some(_) => false
        case None => true
      }
    }
  }

  private def validateUsername(username: String): Future[Either[String, Unit]] =
    usernameIsFree(username).map {
      case true => Right(())
      case false => Left("Username already exists")
    }

//CHECK EMAIL HELPERS
  private def emailInCorrectFormat (email: String): Boolean = {
    val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".r

    emailRegex.findFirstMatchIn(email) match {
      case Some(_) => true
      case None => false
    }
  }

  private def validateEmail(email: String): Either[String, Unit] =
    emailInCorrectFormat(email) match {
      case true => Right(())
      case false => Left("Email address is not in a valid format")
    }


//  CHECK PASSWORD HELPERS
  private def strongPassword (password: String): Boolean = {
    val specialChars = "!?.#$%^&*()@"
    val passwordContainsSpecial = password.exists(char => specialChars.contains(char))
    if (password.length >= 8 && passwordContainsSpecial) true else false
  }

  private def validatePassword(password: String): Either[String, Unit] =
    strongPassword(password) match {
      case true => Right(())
      case false => Left("Password must contain more than 8 characters and a special character")
    }

  private def validateUser(username: String, email: String, password: String): Future[Either[String, Unit]] =
  validateUsername(username).flatMap {
    case Left(error) => Future.successful(Left(error))
    case Right(_) =>
      validateEmail(email) match {
      case Left(error) => Future.successful(Left(error))
      case Right(_) => Future.successful(validatePassword(password))
    }
  }

  def signUp = Action.async(parse.json) { implicit request =>
    val json = request.body.as[JsObject]
    val username = (json \ "username").as[String]
    val email = (json \ "email").as[String]
    val password = (json \ "password").as[String]

    val user = User(None, username, email, password)

    validateUser(username, email, password).flatMap {
      case Left(errorMessage) =>
        Future.successful(BadRequest(Json.obj("status" -> "error", "message" -> errorMessage)))
      case Right(_) =>
        userDAO.addUser(user).map { id =>
          Created(Json.obj("status" -> "success", "message" -> s"User $id created"))
        }.recover {
          case _ => InternalServerError(Json.obj("status" -> "error", "message" -> "User could not be created"))
        }
    }
  }

  def logIn = Action.async(parse.json) { implicit request =>
    (request.body \ "username").asOpt[String].zip((request.body \ "password").asOpt[String]).map {
      case (username, password) =>
        userDAO.findUserByUsername(username).map {
          case Some(user) if BCrypt.checkpw(password, user.password) =>
            Ok(Json.obj("status" -> "success", "message" -> "Logged in"))
          case _ => Unauthorized(Json.obj("status" -> "error", "message" -> "Invalid credentials"))
        }
    }.getOrElse(Future.successful(BadRequest("Invalid login data")))
  }
}

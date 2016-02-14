package controllers

import javax.inject.Inject

import models.User
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.ValidationError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc._
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserController @Inject() (val messagesApi: MessagesApi, userService: UserService)
  extends Controller with I18nSupport {

  private val logger = Logger(this.getClass)

  /** Create user form */
  val createUserForm = Form(
    tuple(
      "firstName" -> nonEmptyText(maxLength = 12),
      "lastName" -> nonEmptyText(maxLength = 12),
      "age" -> number(min = 18, max = 99),
      "active" -> boolean
    )
  )

  def users = Action.async { implicit request =>
    userService.findAll() map { users =>
      logger.info(s"Found ${users.size} users")
      Ok(views.html.users(users, createUserForm))
    }
  }

  /** Handles user form save request */
  def createUser = Action.async { implicit request =>
    val formValidationResult = createUserForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
      // form has validation errors
      userService.findAll() map { users =>
        BadRequest(views.html.users(users,formWithErrors))
      }
    }, { u =>
      // binding successful
      logger.info(s"Request body: ${u.getClass}")
      val user = User(None, firstName = u._1, lastName = u._2, age = u._3, active = u._4)
      userService.insert(user) map {
        case savedUser =>
          logger.info(s"Saved user: $user")
          Redirect(routes.UserController.users()).flashing("message" -> "user saved!")
      }
    })
  }

  /** Handles user save from JSON POST request's */
  def insert() = Action.async(parse.json) { request =>
    request.body.validate[User] fold(invalid = handleValidationErrors, valid = save)
  }

  private def handleValidationErrors: Seq[(JsPath, Seq[ValidationError])] => Future[Result] = { errors =>
    Future {
      logger.error(s"Failed to create a user due to invalid json format: ${JsError.toJson(errors)}")
      BadRequest(s"Invalid json format: ${JsError.toJson(errors)}")
    }
  }

  private def save: User => Future[Result] = { user =>
    userService.insert(user) map {
      case savedUser =>
        logger.info(s"Successfully created user=$savedUser")
        Ok(toJson(savedUser))
    }
  }

}

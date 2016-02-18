package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.i18n.MessagesApi
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global

class UserController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, CookieAuthenticator],
  userService: UserService)
  extends Silhouette[User, CookieAuthenticator] {

  def users = SecuredAction.async { implicit request =>
    userService.findAll() map { users =>
      logger.info(s"Found ${users.size} users")
      Ok(views.html.users(request.identity, users))
    }
  }

}

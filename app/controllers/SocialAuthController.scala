package controllers

import javax.inject.Inject

import _root_.services.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import models.User
import play.api.i18n.{ MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action

import scala.concurrent.Future

class SocialAuthController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, CookieAuthenticator],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, CookieAuthenticator] with Logger {

  /**
    * Authenticates a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticate(provider: String) = Action.async { implicit request =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>

        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) =>
            p.retrieveProfile(authInfo).flatMap { profile =>
              val email = profile.email.get
              if (email.endsWith("cloudwick.com")) {
                for {
                  user <- userService.insert(profile)
                  authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
                  authenticator <- env.authenticatorService.create(profile.loginInfo)
                  value <- env.authenticatorService.init(authenticator)
                  result <- env.authenticatorService.embed(value, Redirect(routes.Application.index()))
                } yield {
                  env.eventBus.publish(LoginEvent(user, request, request2Messages))
                  result
                }
              } else {
                Future(Redirect(routes.Application.signIn()).flashing("error" -> Messages("Use Cloudwick Email Address.")))
              }
            }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.Application.signIn()).flashing("error" -> Messages("could.not.authenticate"))
    }
  }

}

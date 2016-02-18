package services

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User
import play.api.libs.json.JsValue

import scala.concurrent.Future

/**
  * Handles actions to users.
  */
trait UserService extends IdentityService[User] {
  def find(email: String): Future[Option[User]]

  def findAll(): Future[List[User]]

  def insert(user: User): Future[User]

  def insert(profile: CommonSocialProfile): Future[User]

  def remove(email: String): Future[Unit]

  def update(email: String, update: JsValue): Future[User]
}

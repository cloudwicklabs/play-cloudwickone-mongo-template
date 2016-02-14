package dao

import models.User
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait UserDao {
  def find(id: Long): Future[Option[User]]

  def findAll(): Future[List[User]]

  def insert(user: User): Future[User]

  def remove(id: Long): Future[Unit]

  def update(id: Long, update: JsValue): Future[User]
}

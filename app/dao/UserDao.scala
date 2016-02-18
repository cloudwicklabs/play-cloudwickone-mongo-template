package dao

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait UserDao {

  /**
    * Finds a user by its user email.
    *
    * @param email The email of the user to find.
    * @return The found user or None if no user for the given ID could be found.
    */
  def find(email: String): Future[Option[User]]

  /**
    * Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    * @return The found user or None if no user for the given login info could be found.
    */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /**
    * Find all users.
    *
    * @return All users or None if no users are found.
    */
  def findAll(): Future[List[User]]

  /**
    * Saves/Inserts a user.
    *
    * @param user The user to save/insert.
    * @return The saved/inserted user.
    */
  def insert(user: User): Future[User]

  /**
    * Remove a user by its user email
    *
    * @param email The email of the user to delete.
    * @return
    */
  def remove(email: String): Future[Unit]

  /**
    * Update a user by its specified email.
    *
    * @param email The email of the user to update.
    * @param update To be updated value.
    * @return The updated user.
    */
  def update(email: String, update: JsValue): Future[User]
}

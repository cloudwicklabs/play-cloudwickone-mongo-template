package services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json._
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import dao.UserDao
import models.User
import services.UserServiceImpl._
import utils.JodaDateTimeReadWrite._

class UserServiceImpl @Inject()(userDao: UserDao) extends UserService {

  private val logger = Logger(this.getClass)

  override def find(email: String): Future[Option[User]] = userDao.find(email)

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDao.find(loginInfo)

  override def findAll(): Future[List[User]] = userDao.findAll()

  override def insert(user: User): Future[User] = userDao.insert(user)

  override def remove(email: String): Future[Unit] = userDao.remove(email)

  override def update(email: String, update: JsValue): Future[User] =
    userDao.update(email, updateNow(update))

  private def updateNow(update: JsValue): JsObject =
    update.as[JsObject] ++ obj(UpdatedDateField -> Some(DateTime.now))

  override def insert(profile: CommonSocialProfile): Future[User] = {
    userDao.find(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        logger.debug(s"Found user ${user._id} updating details")
        val updateValue = obj(
          FirstNameFiled -> profile.firstName,
          LastNameField -> profile.lastName,
          FullNameField -> profile.fullName,
          AvatarField -> profile.avatarURL
        )
        update(user._id, updateValue)
      case None => // Insert a new user
        logger.debug(s"Inserting new user with email: ${profile.email}")
        userDao.insert(User(
          _id = profile.email.get,
          loginInfo = profile.loginInfo,
          firstName = profile.firstName,
          lastName = profile.lastName,
          fullName = profile.fullName,
          avatarURL = profile.avatarURL
        ))
    }
  }
}

object UserServiceImpl {
  val FirstNameFiled = "firstName"
  val LastNameField = "lastName"
  val FullNameField = "fullName"
  val EmailField = "email"
  val AvatarField = "avatarURL"
  val UpdatedDateField = "updatedDate"
}

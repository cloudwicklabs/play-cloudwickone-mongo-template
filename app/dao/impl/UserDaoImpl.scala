package dao.impl

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import dao.UserDao
import dao.exception.UserDaoException
import models.User
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._ // required for resolving implicit writes
import reactivemongo.bson.BSONObjectID
import reactivemongo.extensions.json.dao.JsonDao
import reactivemongo.extensions.json.dsl.JsonDsl._ // syntactic sugar for querying ex: $eq, etc.
import utils.JodaDateTimeReadWrite._
import scala.concurrent.Future

class UserDaoImpl @Inject() (reactiveMongoApi: ReactiveMongoApi) extends UserDao {

  private lazy val logger = Logger(this.getClass)

  val dao = new JsonDao[User, BSONObjectID](reactiveMongoApi.db, UserDaoImpl.CollectionName) {}

  override def find(email: String): Future[Option[User]] = dao.findOne("_id" $eq email)

  override def find(loginInfo: LoginInfo): Future[Option[User]] = {
    dao.findOne("loginInfo" $eq loginInfo)
  }

  override def findAll(): Future[List[User]] = dao.findAll()

  override def insert(user: User): Future[User] = {
    val enrichedUser = enrichUserWithDate(user)
    logger.debug(s"Inserting user: $enrichedUser")
    dao.insert(enrichedUser) map { result =>
      if (result.ok) user else throw UserDaoException(s"Cannot insert new user $user")
    }
  }

  def enrichUserWithDate(user: User): User = {
    user.copy(
      createdDate = Some(DateTime.now),
      updatedDate = Some(DateTime.now)
    )
  }

  override def remove(email: String): Future[Unit] = {
    dao.remove("_id" $eq email) map { lastError =>
      if (lastError.n == 0) throw UserDaoException(s"Cannot delete user id=$email")
    }
  }

  override def update(email: String, update: JsValue): Future[User] = {
    logger.debug(s"Updating user: $email with document: $update")
    dao.update("_id" $eq email, obj("$set" -> update)) flatMap { lastError =>
      if (lastError.ok) {
        find(email) map {
          case Some(user) => user
          case None =>
            throw UserDaoException(s"Cannot get user id=$email after updating of collection ${UserDaoImpl.CollectionName}")
        }
      }
      else {
        throw UserDaoException(s"Cannot update user with id=$email and update=$update", lastError.getCause)
      }
    }
  }
}

object UserDaoImpl {
  val CollectionName = "users"
}


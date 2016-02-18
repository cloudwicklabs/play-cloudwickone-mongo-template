package models

import akka.util.ByteString
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import org.joda.time.DateTime
import play.api.libs.json.Json
import redis.ByteStringFormatter

case class User(
  _id: String, // email
  loginInfo: LoginInfo,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  avatarURL: Option[String],
  createdDate: Option[DateTime] = Some(DateTime.now),
  updatedDate: Option[DateTime] = Some(DateTime.now)
) extends Identity

object User {
  implicit val jsonFormat = Json.format[User]

  implicit val byteStringFormat = new ByteStringFormatter[User] {
    def serialize(data: User): ByteString = {
      ByteString(Json.toJson(data).toString)
    }

    def deserialize(bs: ByteString): User = {
      val s = bs.utf8String
      Json.fromJson[User](Json.parse(s)).get
    }
  }
}
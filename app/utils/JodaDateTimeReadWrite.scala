package utils

import org.joda.time.DateTime
import reactivemongo.bson.{BSONDateTime, BSONHandler}

/**
  * Conversion for Joda's DateTime
  */
object JodaDateTimeReadWrite {
  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdTime: DateTime) = BSONDateTime(jdTime.getMillis)
  }
}

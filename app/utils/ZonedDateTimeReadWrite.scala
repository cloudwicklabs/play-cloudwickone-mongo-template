package utils

import java.time.ZonedDateTime

import play.api.data.validation.ValidationError
import play.api.libs.json.Json._
import play.api.libs.json._
import utils.Time._

import scala.util.{Failure, Success, Try}

object ZonedDateTimeReadWrite {

  implicit val zonedDateTimeReads: Reads[ZonedDateTime] = new Reads[ZonedDateTime] {
    override def reads(json: JsValue): JsResult[ZonedDateTime] = json match {
      case js: JsObject => convertJsonToJsResultDate(js)
      case JsString(v) => convertStringToJsResultDate(v)
      case JsNumber(v) => convertMillisToJsResultDate(v.longValue())
      case _ => invalidDate
    }
  }

  implicit val zonedDateTimeWrites: Writes[ZonedDateTime] = new Writes[ZonedDateTime] {
    override def writes(d: ZonedDateTime): JsValue = {
      obj("$date" -> getEpochMillis(d))
    }
  }

  private def convertJsonToJsResultDate(json: JsObject): JsResult[ZonedDateTime] =
    wrapWithJsResult(parseDateFromJs(json))

  private def convertStringToJsResultDate(v: String): JsResult[ZonedDateTime] =
    wrapWithJsResult(Try(parseDate(v)))

  private def convertMillisToJsResultDate(v: Long): JsResult[ZonedDateTime] =
    wrapWithJsResult(Try(parseDate(v)))

  private def getEpochMillis(d: ZonedDateTime): JsNumber =
    JsNumber(d.toInstant.toEpochMilli)

  private def wrapWithJsResult(v: Try[ZonedDateTime]) = v match {
    case Success(d) => JsSuccess(d)
    case Failure(e) => JsError(Seq(JsPath() -> Seq(
      ValidationError("validate.error.expected.date.isoformat", s"${e.getMessage}"))))
  }

  private def parseDateFromJs(js: JsObject): Try[ZonedDateTime] = Try {
    js \ "$date" match {
      case JsDefined(v) => v match {
        case JsNumber(num) => parseDate(num.longValue())
        case _ => throw UnsupportedDateFormatException()
      }
      case _ => throw UnsupportedDateFormatException()
    }
  }

  private val invalidDate = JsError(
    Seq(JsPath() -> Seq(ValidationError("validate.error.expected.date"))))
}



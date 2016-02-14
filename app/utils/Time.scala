package utils

import java.time.{Instant, ZonedDateTime, ZoneId}
import java.time.format.DateTimeFormatter

object Time {

  val utcZoneId = ZoneId.of("UTC")

  val sgtZoneId = ZoneId.of("Singapore")

  private val formatter = DateTimeFormatter.ISO_DATE_TIME

  def parseDate(str: String): ZonedDateTime = ZonedDateTime.parse(str, formatter)

  def parseDate(epochMillis: Long): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), utcZoneId)

  /** Add extension methods to ZonedDateTime */
  implicit class TimeComparator(z: ZonedDateTime) {
    def >=(comp: ZonedDateTime): Boolean = {
      z.toInstant.toEpochMilli >= comp.toInstant.toEpochMilli
    }

    def is(comp: ZonedDateTime): Boolean = {
      z.toInstant.toEpochMilli == comp.toInstant.toEpochMilli
    }

    def <=(comp: ZonedDateTime): Boolean = {
      z.toInstant.toEpochMilli <= comp.toInstant.toEpochMilli
    }

    def <(comp: ZonedDateTime): Boolean = {
      z.toInstant.toEpochMilli < comp.toInstant.toEpochMilli
    }
  }

  case class UnsupportedDateFormatException() extends RuntimeException

}
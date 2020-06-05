package learn.common.util

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

object TimeUtils extends Serializable {
  lazy val _formatterTime: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  def formatTime(inst: TemporalAccessor): String = _formatterTime.format(inst)

  def formatTime(epochMill: Long): String = formatTime(Instant.ofEpochMilli(epochMill))
}

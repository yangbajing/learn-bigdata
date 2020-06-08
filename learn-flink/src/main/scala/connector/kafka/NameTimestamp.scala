package connector.kafka

import java.time.Instant

import learn.common.util.TimeUtils

case class NameTimestamp(name: String, t: Instant, seq: Int) {
//  override def toString: String = s"NameTimestamp($name, ${TimeUtils.formatTime(t)}, $seq)"
}

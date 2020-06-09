package connector.kafka

case class NameTimestamp(name: String, t: java.sql.Timestamp, seq: Int) {
//  override def toString: String = s"NameTimestamp($name, ${TimeUtils.formatTime(t)}, $seq)"
}

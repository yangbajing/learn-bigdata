import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

val s = "2015-03-18T00:00:00-08:30"

s.take(19)

LocalDateTime.parse(s, format)


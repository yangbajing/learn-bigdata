package learn.kafka.streams

import java.time.Duration
import java.util.Properties
import java.util.concurrent.CountDownLatch

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.{ KafkaStreams, StreamsBuilder, StreamsConfig }

/**
 * @author yangjing (yang.xunjing@qq.com)
 * @date 2020-09-02 15:41
 */
object ReadNginxLog {
  case class LogLine(payload: String, schema: JsonNode)

  private val objectMapper = new ObjectMapper().findAndRegisterModules()

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "os-check-streams")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_WINDOWED_KEY_SERDE_INNER_CLASS, classOf[Serdes.StringSerde].getName)

    val builder = new StreamsBuilder()

    val source: KStream[String, String] = builder.stream("access_log")

    source
      .mapValues[LogLine](new ValueMapper[String, LogLine] {
        override def apply(value: String): LogLine = objectMapper.readValue(value, classOf[LogLine])
      })
      .mapValues[String](new ValueMapper[LogLine, String] {
        override def apply(value: LogLine): String = value.payload
      })
      .groupBy((key, value) => if (value.contains("Chrome")) "Chrome" else "Firefox")
      .windowedBy(TimeWindows.of(Duration.ofSeconds(5)))
      .count()
      .toStream()
      .to("browser-check", Produced.`with`(WindowedSerdes.timeWindowedSerdeFrom(classOf[String]), Serdes.Long()))

    val topology = builder.build()

    val streams = new KafkaStreams(topology, props)
    val latch = new CountDownLatch(1)

    Runtime.getRuntime.addShutdownHook(new Thread("streams-shutdown-hook") {
      override def run(): Unit = {
        streams.close()
        latch.countDown()
      }
    })

    streams.start()
    latch.await()
  }
}

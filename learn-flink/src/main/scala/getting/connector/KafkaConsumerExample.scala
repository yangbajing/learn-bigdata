package getting.connector

import java.util.Properties

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

object KafkaConsumerExample {
  def main(args: Array[String]): Unit = {
    val topic = "test-json"

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(5000) // 每隔 5000 毫秒 执行一次 checkpoint，可启用容错的 Kafka Consumer
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", topic)
    properties.setProperty("isolation.level", "read_committed")
//    properties.setProperty("flink.partition-discovery.interval-millis", "5000") // Kafka 分区自动发现间隔（秒）
    val consumer =
      new FlinkKafkaConsumer[NameTimestamp](topic, new NameTimestampDeserializationSchema(), properties)
//    consumer.setStartFromEarliest()      // 尽可能从最早的记录开始
//    consumer.setStartFromLatest() // 从最新的记录开始
//    consumer.setStartFromTimestamp(System.currentTimeMillis())  // 从指定的时间开始（毫秒）
//    consumer.setStartFromGroupOffsets()  // 默认的方法
    consumer.assignTimestampsAndWatermarks(new CustomWatermarkEmitter())

    val stream = env.addSource(consumer).print()

    env.execute("Kafka Consumer Example")
  }

  private class CustomWatermarkEmitter(maxOutOfOrderness: Long = 1000L)
      extends AssignerWithPeriodicWatermarks[NameTimestamp] {
    private var curMaxTS = 0L
    override def getCurrentWatermark: Watermark = new Watermark(curMaxTS - maxOutOfOrderness)

    override def extractTimestamp(element: NameTimestamp, previousElementTimestamp: Long): Long = {
      val ts = element.t.toEpochMilli
      curMaxTS = math.max(curMaxTS, ts)
      ts
    }
  }

  private class NameTimestampDeserializationSchema() extends AbstractDeserializationSchema[NameTimestamp] {
    private lazy val mapper = new ObjectMapper().findAndRegisterModules()
    override def deserialize(message: Array[Byte]): NameTimestamp = {
      mapper.readValue(message, classOf[NameTimestamp])
    }
  }
}

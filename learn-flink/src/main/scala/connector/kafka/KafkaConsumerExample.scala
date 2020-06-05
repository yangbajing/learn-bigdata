package connector.kafka

import java.util.Properties

import com.fasterxml.jackson.databind.ObjectMapper
import learn.common.util.TimeUtils
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.util.Collector

object KafkaConsumerExample {
  val WATERMARK_INTERVAL: Long = 10 * 1000
  def main(args: Array[String]): Unit = {
    val topic = "test-json"

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(5000) // 每隔 5000 毫秒 执行一次 checkpoint，可启用容错的 Kafka Consumer
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.getConfig.setAutoWatermarkInterval(200)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", topic)
    properties.setProperty("isolation.level", "read_committed")
//    properties.setProperty("flink.partition-discovery.interval-millis", "5000") // Kafka 分区自动发现间隔（秒）
    val consumer =
      new FlinkKafkaConsumer[NameTimestamp](topic, new NameTimestampDeserializationSchema(), properties)
      //    .setStartFromEarliest()      // 尽可能从最早的记录开始
      //    .setStartFromLatest() // 从最新的记录开始
      //    .setStartFromTimestamp(System.currentTimeMillis())  // 从指定的时间开始（毫秒）
      //    .setStartFromGroupOffsets()  // 默认的方法
        .assignTimestampsAndWatermarks(new CustomWatermarkEmitter())

    val lateData = new OutputTag[NameTimestamp]("LateData")

    val stream = env
      .addSource(consumer)
      .windowAll(TumblingEventTimeWindows.of(Time.seconds(5)))
      .sideOutputLateData(lateData)
      .process(new ProcessAllWindowFunction[NameTimestamp, List[Int], TimeWindow] {
        override def process(context: Context, elements: Iterable[NameTimestamp], out: Collector[List[Int]]): Unit = {
          val value = elements.map(_.seq).toList
          println(s"[${context.window.getStart} ${context.window.getEnd}), $value")
          out.collect(value.sorted)
        }
      })
      .name("window-process")

    stream.print()

    stream.getSideOutput(lateData).map { nt =>
      println(s"延迟数据：$nt")
      nt
    }

    env.execute("Kafka Consumer Example")
  }

  private class CustomWatermarkEmitter extends AssignerWithPeriodicWatermarks[NameTimestamp] {
    @volatile private var curMaxTS = 0L
    private var wm: Watermark = _
    override def getCurrentWatermark: Watermark = {
      wm = new Watermark(curMaxTS /*- WATERMARK_INTERVAL*/ )
      wm
    }

    override def extractTimestamp(element: NameTimestamp, previousElementTimestamp: Long): Long = {
      val ts = element.t.toEpochMilli
//      curMaxTS = if (ts > curMaxTS) curMaxTS + WATERMARK_INTERVAL else curMaxTS
      curMaxTS = math.max(curMaxTS, ts)
      println(s"$element, $curMaxTS, $wm")
      ts
    }
  }

  private class NameTimestampDeserializationSchema() extends AbstractDeserializationSchema[NameTimestamp] {
    private lazy val mapper = new ObjectMapper().findAndRegisterModules()
    override def deserialize(message: Array[Byte]): NameTimestamp = {
      val value = mapper.readValue(message, classOf[NameTimestamp])
//      println("Receive: " + value)
      value
    }
  }
}

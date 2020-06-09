package connector.kafka

import java.util.Properties
import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema
import org.apache.flink.api.java.io.jdbc.{JDBCOutputFormat, JDBCOutputFormatSinkFunction}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.types.Row
import org.apache.flink.util.Collector
import org.slf4j.LoggerFactory

object KafkaConsumerExample {
  val WATERMARK_INTERVAL: Long = TimeUnit.SECONDS.toMillis(2)

  def main(args: Array[String]): Unit = {
    val topic = "test-json"

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(5000) // 每隔 5000 毫秒 执行一次 checkpoint，可启用容错的 Kafka Consumer
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)
    env.getConfig.setAutoWatermarkInterval(WATERMARK_INTERVAL)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", topic)
    properties.setProperty("isolation.level", "read_committed")
//    properties.setProperty("flink.partition-discovery.interval-millis", "5000") // Kafka 分区自动发现间隔（秒）
    val consumer =
      new FlinkKafkaConsumer[NameTimestamp](topic, new NameTimestampDeserializationSchema(), properties)
      //    // 尽可能从最早的记录开始
      //    .setStartFromEarliest()
      //    // 从最新的记录开始
      //    .setStartFromLatest()
      //    // 从指定的时间开始（毫秒）
      //    .setStartFromTimestamp(System.currentTimeMillis())
      //    // 默认的方法
      //    .setStartFromGroupOffsets()
        .assignTimestampsAndWatermarks(new CustomWatermarkEmitter())

//    val lateData = new OutputTag[NameTimestamp]("LateData")

    val stream = env
      .addSource(consumer)
      .windowAll(TumblingEventTimeWindows.of(Time.seconds(5)))
      .allowedLateness(Time.hours(1))
      //      .sideOutputLateData(lateData)
      .process(new CustomWindowProcess())
      .name("window-process")

    val jdbcOutputFormat = JDBCOutputFormat
      .buildJDBCOutputFormat()
      .setDrivername("com.mysql.cj.jdbc.Driver")
      .setDBUrl("jdbc:mysql://localhost:3306/bigdata?serverTimezone=Asia/Shanghai")
      .setUsername("bigdata")
      .setPassword("Bigdata.2020")
      .setBatchInterval(5000)
      .setQuery("insert into name_test(name, t, seq) values(?, ?, ?)")
      .setSqlTypes(Array(java.sql.Types.VARCHAR, java.sql.Types.TIMESTAMP, java.sql.Types.INTEGER))
      .finish()
    val rowStream = stream.flatMap(v => v).map { v =>
      val row = new Row(3)
      row.setField(0, v.name)
      row.setField(1, v.t)
      row.setField(2, v.seq)
      row
    }
    rowStream.addSink(new JDBCOutputFormatSinkFunction(jdbcOutputFormat)).name("sink-to-mysql")

//    stream.print()

//    stream.getSideOutput(lateData).map { nt =>
//      println(s"延迟数据：$nt")
//      nt
//    }
//    tableEnv.execute("Kafka Consumer insert to MySQL")

    env.execute("Kafka Consumer Example")
  }
}

class CustomWindowProcess extends ProcessAllWindowFunction[NameTimestamp, Seq[NameTimestamp], TimeWindow] {
  private val logger = LoggerFactory.getLogger(getClass)

  override def process(
      context: Context,
      elements: Iterable[NameTimestamp],
      out: Collector[Seq[NameTimestamp]]): Unit = {
    val value = elements.toList
    logger.debug(s"[${context.window.getStart} ${context.window.getEnd}), ${value.map(_.seq)}")
    out.collect(value)
  }
}

class CustomWatermarkEmitter extends AssignerWithPeriodicWatermarks[NameTimestamp] {
  private val logger = LoggerFactory.getLogger(getClass)
  private val outLatenessTS = 2000L
  private var curMaxTS = 0L
  private var wm: Watermark = _

  override def getCurrentWatermark: Watermark = {
    wm = new Watermark(curMaxTS - outLatenessTS)
    logger.trace(s"Generate watermark is [$wm].")
    wm
  }

  override def extractTimestamp(element: NameTimestamp, previousElementTimestamp: Long): Long = {
    val ts = element.t.getTime
    //      curMaxTS = if (ts > curMaxTS) curMaxTS + WATERMARK_INTERVAL else curMaxTS
    curMaxTS = math.max(curMaxTS, ts)
    logger.debug(s"$element, $curMaxTS, $wm")
    ts
  }
}

class NameTimestampDeserializationSchema() extends AbstractDeserializationSchema[NameTimestamp] {
  private lazy val mapper = new ObjectMapper().findAndRegisterModules()
  override def deserialize(message: Array[Byte]): NameTimestamp = mapper.readValue(message, classOf[NameTimestamp])
}

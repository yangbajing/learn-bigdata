package connector.kafka

import java.io.Serializable
import java.time.Instant
import java.util.Properties
import java.util.concurrent.{ ThreadLocalRandom, TimeUnit }

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.functions.source.FromIteratorFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic
import org.apache.flink.streaming.connectors.kafka.{ FlinkKafkaProducer, KafkaContextAware, KafkaSerializationSchema }
import org.apache.kafka.clients.producer.ProducerRecord

import scala.util.Random

object KafkaProducerExample {
  val CHECK_POINT_INTERVAL = 5000

  def main(args: Array[String]): Unit = {
    val topic = "test-json"

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Semantic.EXACTLY_ONCE 需要启用检查点
    env.enableCheckpointing(CHECK_POINT_INTERVAL, CheckpointingMode.EXACTLY_ONCE)

    // transactional.id 由 flink 统一提供
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("enable.idempotence", "true")
    // 启用查检点后抛异常：Interrupted while joining ioThread java.lang.InterruptedException
    // 设置 transaction.timeout.ms 为 15 分钟无问题
    properties.setProperty("transaction.timeout.ms", s"${60 * 5 * 1000}")

    val producer = new FlinkKafkaProducer[NameTimestamp](
      topic,
      new NameTimestampSerializationSchema(topic),
      properties,
      Semantic.EXACTLY_ONCE)

    // 0.10+ 版本的 Kafka 允许在将记录写入 Kafka 时附加记录的事件时间戳；此方法不适用于早期版本的 Kafka
    producer.setWriteTimestampToKafka(true)

    val stream = env.addSource(new NameTimestampSource()).name("name-source").flatMap(option => option.toSeq)
    stream.addSink(producer).name("name-sink")

    env.execute("Kafka Producer Example")
  }

  class NameTimestampSource() extends FromIteratorFunction[Option[NameTimestamp]](new RateLimitedIterator()) {}

  @SerialVersionUID(1L)
  private class RateLimitedIterator() extends java.util.Iterator[Option[NameTimestamp]] with Serializable {
    private val MAX = 20
    private var count = 0
    override def hasNext: Boolean = count <= MAX

    override def next: Option[NameTimestamp] = {
      count += 1
      count match {
        case 21 =>
          // 等待 checkpoint 执行
          Thread.sleep(CHECK_POINT_INTERVAL * 2)
          None
        case _ =>
          Thread.sleep(ThreadLocalRandom.current().nextLong(200, 1000))
          Some(NameTimestamp(Vector.fill(8)(Random.nextPrintableChar).mkString, Instant.now(), count))
      }
    }
  }

  private class NameTimestampSerializationSchema(topic: String)
      extends KafkaSerializationSchema[NameTimestamp]
      with KafkaContextAware[NameTimestamp] {
    private lazy val mapper = new ObjectMapper().findAndRegisterModules()
    private var parallelInstanceId: Int = _
    private var numParallelInstances: Int = _
    private var partitions: Array[Int] = _

    override def serialize(
        element: NameTimestamp,
        timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      println(element)
      new ProducerRecord(topic, null, element.t.toEpochMilli, null, mapper.writeValueAsBytes(element))
    }

    override def getTargetTopic(element: NameTimestamp): String = topic

    override def setParallelInstanceId(parallelInstanceId: Int): Unit = this.parallelInstanceId = parallelInstanceId

    override def setNumParallelInstances(numParallelInstances: Int): Unit =
      this.numParallelInstances = numParallelInstances

    override def setPartitions(partitions: Array[Int]): Unit = this.partitions = partitions
  }
}

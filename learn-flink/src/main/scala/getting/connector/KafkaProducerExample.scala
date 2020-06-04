package getting.connector

import java.io.Serializable
import java.util.Properties

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.shaded.netty4.io.netty.util.internal.ThreadLocalRandom
import org.apache.flink.streaming.api.functions.source.FromIteratorFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{ FlinkKafkaProducer, KafkaSerializationSchema }
import org.apache.kafka.clients.producer.ProducerRecord

import scala.util.Random

object KafkaProducerExample {
  def main(args: Array[String]): Unit = {
    val topic = "test-json"

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(5000) // 启用查检点后抛异常：Interrupted while joining ioThread java.lang.InterruptedException

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("transactional.id", "test-001")
    val producer = new FlinkKafkaProducer[NameTimestamp](
      topic, // 目标 topic
      new NameTimestampSerializationSchema(topic),
      properties,
      FlinkKafkaProducer.Semantic.EXACTLY_ONCE)

    // 0.10+ 版本的 Kafka 允许在将记录写入 Kafka 时附加记录的事件时间戳；
    // 此方法不适用于早期版本的 Kafka
    producer.setWriteTimestampToKafka(true)

    val stream = env.addSource(new NameTimestampSource()).name("transaction")

    stream.addSink(producer)

    env.execute("Kafka Producer Example")
  }

  class NameTimestampSource() extends FromIteratorFunction[NameTimestamp](new RateLimitedIterator()) {}

  @SerialVersionUID(1L)
  private class RateLimitedIterator() extends java.util.Iterator[NameTimestamp] with Serializable {
    private val MAX = 20
    private var count = 0
    override def hasNext: Boolean = count < MAX

    override def next: NameTimestamp = {
      Thread.sleep(ThreadLocalRandom.current().nextLong(100, 1000))
      count += 1
      NameTimestamp(Vector.fill(8)(Random.nextPrintableChar).mkString, System.currentTimeMillis())
    }
  }

//  private class NameTimestampSerializationSchema(topic: String) extends KeyedSerializationSchema[NameTimestamp] {
//    private lazy val mapper = new ObjectMapper().findAndRegisterModules()
//
//    override def serializeKey(element: NameTimestamp): Array[Byte] = null
//
//    override def serializeValue(element: NameTimestamp): Array[Byte] = mapper.writeValueAsBytes(element)
//
//    override def getTargetTopic(element: NameTimestamp): String = topic
//  }
  private class NameTimestampSerializationSchema(topic: String) extends KafkaSerializationSchema[NameTimestamp] {
    private lazy val mapper = new ObjectMapper().findAndRegisterModules()
    override def serialize(
        element: NameTimestamp,
        timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      new ProducerRecord(topic, null, timestamp, null, mapper.writeValueAsBytes(element))
    }
  }
}

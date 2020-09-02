package receiving.service

import java.util.{ Objects, Properties }

import org.apache.kafka.clients.producer.{ Callback, KafkaProducer, ProducerRecord, RecordMetadata }

import scala.concurrent.{ Future, Promise }

class KafkaService {
  private val producer = createProducer()

  def send(topic: String, bytes: Array[Byte], key: String = null): Future[RecordMetadata] = {
    val promise = Promise[RecordMetadata]()
    producer.send(new ProducerRecord[String, Array[Byte]](topic, key, bytes), new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit =
        if (Objects.isNull(exception)) promise.success(metadata)
        else promise.failure(exception)
    })
    promise.future
  }

  def createProducer(): KafkaProducer[String, Array[Byte]] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("acks", "all")
    props.put("retries", "0")
    props.put("batch.size", "16384")
    props.put("linger.ms", "1")
    props.put("buffer.memory", "33554432")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
    new KafkaProducer[String, Array[Byte]](props)
  }
}

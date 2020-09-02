package receiving.service

import java.io.ByteArrayOutputStream

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import learn.avro.CustomDatumWriter
import org.apache.avro.Schema
import org.apache.avro.file.{ DataFileReader, DataFileWriter, SeekableByteArrayInput }
import org.apache.avro.generic.{ GenericDatumReader, GenericRecord }
import org.apache.avro.io.DecoderFactory
import receiving.model.JsonResult
import receiving.util.KafkaUtils

import scala.concurrent.{ ExecutionContext, Future }

class ReceivingService extends StrictLogging {
  private val kafkaService = new KafkaService()

  def process(request: HttpRequest)(implicit mat: Materializer): Future[JsonResult] = {
    import mat.executionContext
    val topic = KafkaUtils.generateTopic(request.uri.path.tail)
    request.entity.contentType.mediaType match {
      case mediaType if mediaType.isText =>
        Unmarshal(request.entity).to[String].flatMap(json => processJSON(topic, json))
      case mediaType if mediaType.binary =>
        Unmarshal(request.entity).to[Array[Byte]].flatMap(bytes => processAvro(topic, bytes))
      case mediaType =>
        Future.failed(new IllegalArgumentException(s"请求内容类型无效，只允许 application/json 或 二级进数据，当前传入为：$mediaType."))
    }
  }

  def processAvro(topic: String, data: Array[Byte])(implicit ec: ExecutionContext): Future[JsonResult] = {
    val schema: Schema = null // From database
    val datumReader = new GenericDatumReader[GenericRecord](schema)
    val reader = DataFileReader.openReader(new SeekableByteArrayInput(data), datumReader)

    processAndSend(topic, schema, writer => reader.forEachRemaining(record => writer.append(record)))
  }

  def processJSON(topic: String, json: String)(implicit ec: ExecutionContext): Future[JsonResult] = {
    val schema: Schema = null // From database
    val datumReader = new GenericDatumReader[GenericRecord](schema)
    val decoder = DecoderFactory.get().jsonDecoder(schema, json)
    val record = datumReader.read(null, decoder)

    processAndSend(topic, schema, writer => writer.append(record))
  }

  def processAndSend(topic: String, schema: Schema, writerFunc: DataFileWriter[GenericRecord] => Unit)(
      implicit ec: ExecutionContext): Future[JsonResult] = {
    val dataFileWriter = new DataFileWriter(new CustomDatumWriter[GenericRecord](schema))
    val out = new ByteArrayOutputStream()
    dataFileWriter.create(schema, out)
    writerFunc(dataFileWriter)
    dataFileWriter.close()

    val bytes = out.toByteArray
    kafkaService.send(topic, bytes).map(_ => JsonResult.ok()).recover {
      case e =>
        logger.error("Send data (json) to kafka error.", e)
        JsonResult.error(e.getMessage)
    }
  }
}

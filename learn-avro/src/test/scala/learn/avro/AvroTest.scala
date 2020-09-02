package learn.avro

import java.io.{ ByteArrayOutputStream, File }
import java.nio.file.Files

import com.zq.avro.{ Sex, SubData, User }
import org.apache.avro.Schema
import org.apache.avro.file.{ DataFileReader, DataFileWriter, SeekableByteArrayInput }
import org.apache.avro.generic.{ GenericData, GenericDatumReader, GenericRecord }
import org.apache.avro.io.{ DecoderFactory, EncoderFactory }
import org.apache.avro.specific.{ SpecificData, SpecificDatumReader }
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

class AvroTest extends AnyWordSpec with Matchers with BeforeAndAfter {
  private val logger = LoggerFactory.getLogger(getClass)
  private val USER_AVSC = "user.avsc"
  private var schema: Schema = _

  before {
    val schema = new Schema.Parser().parse(getClass.getClassLoader.getResourceAsStream(USER_AVSC))
    //printSchema(schema)
    this.schema = schema
  }

  "Without code generation" should {
    val file = new File("/tmp/users-nocode.avro")
    "Serialize" in {
      val specificData = SpecificData.get() //SpecificData.getForSchema(schema)
      val SexSchema = schema.getField("sex").schema()
      val SubSchema = schema.getField("data").schema()

      val user1 = new GenericData.Record(schema)
      user1.put("name", "羊八井")
      user1.put("dataSchema", "Data schema.")
      user1.put("favoriteColor", "blue")
      user1.put("sex", specificData.createEnum("NONE", SexSchema))
      val data1 = new GenericData.Record(SubSchema)
      data1.put("name", "开外挂")
      data1.put("age", 34)
      user1.put("data", data1)
      val user2 = new GenericData.Record(schema)
      user2.put("name", "羊八井杨景")
      user2.put("dataSchema", "Data schema.")
      user2.put("favoriteNumber", 255.3)
      user2.put("sex", specificData.createEnum("MALE", SexSchema))
      val data2 = new GenericData.Record(SubSchema)
      data2.put("name", "开外挂")
      data2.put("age", -1)
      user2.put("data", data2)

      val dataFileWriter = new DataFileWriter(new CustomDatumWriter[GenericRecord](schema))
      dataFileWriter.create(schema, file)
      dataFileWriter.append(user1)
      dataFileWriter.append(user2)
      dataFileWriter.close()
    }

    "Deserialize" in {
      val dataFileReader = new DataFileReader(file, new GenericDatumReader[GenericRecord](schema))
      dataFileReader.forEachRemaining { user =>
        logger.info(user.toString)
      }
    }
  }

  "With code generation" should {
    val file = new File("/tmp/users-code.avro")
    "Serialize" in {
      val user1 =
        User
          .newBuilder()
          .setName("羊八井")
          .setDataSchema("Data Schema")
          .setFavoriteNumber(null)
          .setFavoriteColor("blue")
          .setSex(Sex.NONE)
          .setData(SubData.newBuilder().setName("一").setAge(3).build())
          .build()
      val user2 = User
        .newBuilder()
        .setName("超过5个字符")
        //        .setName("杨景")
        .setDataSchema("")
        .setFavoriteNumber(255.3)
        .setFavoriteColor("yellow")
        .setSex(Sex.MALE)
        .setData(SubData.newBuilder().setName("哈哈哈").setAge(8).build())
        .build()

      val outArr = new ByteArrayOutputStream()

      val datumWriter = new CustomDatumWriter[User](schema, GenericData.get(), true)
      val dataFileWriter = new DataFileWriter(datumWriter)
      dataFileWriter.create(schema, outArr)
      dataFileWriter.append(user1)
      dataFileWriter.append(user2)

      datumWriter.errors.foreach(println)
//      datumWriter.errors shouldBe empty

      dataFileWriter.close()

      val data = outArr.toByteArray
      val reader =
        new DataFileReader(new SeekableByteArrayInput(data), new GenericDatumReader[GenericRecord]( /*schema*/ ))
      reader.forEachRemaining(record => println(s"From byte array read record is $record"))

      println(s"Byte array size is ${outArr.size()}")
      Files.write(file.toPath, data)
    }

    "Deserialize" in {
      val dataFileReader = new DataFileReader(file, new SpecificDatumReader[User](schema))
      dataFileReader.forEachRemaining { user =>
        user.getName.toString should (be("羊八井") or be("杨景"))
        logger.info(user.toString)
      }
    }
  }

  "Json & Avro" should {
    "Serialize" in {
      val user1 =
        User
          .newBuilder()
          .setName("羊八井")
          .setDataSchema("Data Schema")
          .setFavoriteNumber(null)
          .setFavoriteColor("blue")
          .setSex(Sex.NONE)
          .setData(SubData.newBuilder().setName("一").setAge(3).build())
          .build()

      val outArr = new ByteArrayOutputStream()

      val datumWriter = new CustomDatumWriter[User](schema, GenericData.get(), true)
      val encoder = EncoderFactory.get().jsonEncoder(schema, outArr)
      datumWriter.write(user1, encoder)
      encoder.flush()

      println(s"Byte array size is ${outArr.size()}")
      println(outArr.toString)
    }

    "Deserialize Class" in {
      val json =
        """{"name":"羊八井","dataSchema":"Data Schema","favoriteNumber":null,"favoriteColor":{"string":"blue"},"sex":"NONE","data":{"name":"一","age":3}}"""
      val datumReader = new SpecificDatumReader[User](schema)
      val decoder = DecoderFactory.get().jsonDecoder(schema, json)
      val user = datumReader.read(null, decoder)
      println("User class is " + user)
    }

    "Deserialize Generic" in {
      val json =
        """{"name":"羊八井","dataSchema":"Data Schema","favoriteNumber":null,"favoriteColor":{"string":"blue"},"sex":"NONE","data":{"name":"一","age":3}}"""
      val datumReader = new GenericDatumReader[GenericRecord](schema)
      val decoder = DecoderFactory.get().jsonDecoder(schema, json)
      val record = datumReader.read(null, decoder)
      println("Generic record is " + record)
    }
  }

  private def printSchema(schema: Schema): Unit = {
    schema.getFields.forEach { field =>
      val scm = field.schema()
      val types = scm.getType match {
        case Schema.Type.MAP   => scm.getValueType.toString()
        case Schema.Type.UNION => scm.getTypes.asScala.map(_.getType).mkString(",")
        case Schema.Type.ARRAY => scm.getElementType.toString()
        case Schema.Type.ENUM  => scm.getEnumSymbols.asScala.mkString(",")
        case _                 => scm.getType.toString
      }
      logger.debug(s"name: ${field.name()}, type: ${scm.getType} [$types], pos: ${field.pos()}")
    }
  }
}

package learnspark.connectmongodb

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.Date

import com.mongodb.BasicDBObject
import com.mongodb.hadoop.{MongoInputFormat, MongoOutputFormat}
import org.apache.hadoop.conf.Configuration
import org.apache.spark.{SparkContext, SparkConf}
import org.bson.BSONObject

/**
 * Connect MongoDB
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-08-22.
 */
object Sample {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("SparkExample").setMaster("local")
    val sc = new SparkContext(sparkConf)

    val mongoConfig = new Configuration()
    mongoConfig.set("mongo.input.uri", "mongodb://localhost:27017/sc_activity.activityRegistration")

    val documents = sc.newAPIHadoopRDD(
      mongoConfig,
      classOf[MongoInputFormat],
      classOf[Object],
      classOf[BSONObject])

    val outputConfig = new Configuration()
    outputConfig.set("mongo.output.uri", "mongodb://localhost:27017/sc_activity.output")

    val result = documents.map { case (oid, doc) =>
      val o = new BasicDBObject()
      o.put("name", Sample.str(doc.get("name")))
      o.put("job", Sample.str(doc.get("job")))
      oid -> o
    }

    result.saveAsNewAPIHadoopFile(
      "file://this-is-completely-unused",
      classOf[Object],
      classOf[BSONObject],
      classOf[MongoOutputFormat[Object, BSONObject]],
      outputConfig)

    result.foreach(println)
  }

  def parseDocument(dbo: BSONObject) = {
    (str(dbo.get("_id")),
      str(dbo.get("activityId")),
      str(dbo.get("name").toString),
      str(dbo.get("phone").toString),
      str(dbo.get("corporation")),
      str(dbo.get("job")),
      str(dbo.get("createdAt")))
  }

  def str(o: Object) = o match {
    case null => ""
    case d: Date => formatDate.format(d)
    case ld: LocalDateTime => ld.format(formatterDateTime)
    case s => s.toString.trim
  }

  def ld(o: Object) = o match {
    case d: Date => LocalDateTime.ofInstant(Instant.ofEpochMilli(d.getTime), ZoneId.systemDefault())
    case d: LocalDateTime => d
    case _ => LocalDateTime.MIN
  }

  val formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
  val formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
}

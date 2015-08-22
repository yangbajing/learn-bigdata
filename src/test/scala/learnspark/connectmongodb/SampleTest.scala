package learnspark.connectmongodb

import com.mongodb.BasicDBObject
import com.mongodb.hadoop.{MongoOutputFormat, MongoInputFormat}
import org.apache.hadoop.conf.Configuration
import org.apache.spark.{SparkContext, SparkConf}
import org.bson.BSONObject
import org.scalatest.WordSpec

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-08-22.
 */
class SampleTest extends WordSpec {
  val sparkConf = new SparkConf().setAppName("SparkExample").setMaster("local")
  val sc = new SparkContext(sparkConf)

  "SampleTest" should {
    "main" in {
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
  }


}

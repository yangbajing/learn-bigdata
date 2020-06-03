package learnspark.intro

import org.apache.spark._

/**
 * First App for wordcount
 * Created by yangjing on 15-7-23.
 */
object FirstApp {
  def main(args: Array[String]): Unit = {
    val inputFile = sys.env.getOrElse("SPARK_HOME", "/opt/local/spark-2.4.5-bin-hadoop-scala-2.12") + "/README.md"
    val outputFile = "/tmp/learnspark/firstApp"

    val conf = new SparkConf()
      .setAppName("WordCount")
      .setMaster("spark://localhost:7077")
      .set("spark.cores.max", "2")
      .set("spark.driver.userClassPathFirst", "true")
    val sc = new SparkContext(conf)

    val input = sc.textFile(inputFile)
    val words = input.flatMap(_.split(" "))
    //    words.collect().foreach(println)
    val counts =
      words.map(word => (word, 1)).reduceByKey { case (x, y) => x + y }
    counts.saveAsTextFile(outputFile)
  }
}

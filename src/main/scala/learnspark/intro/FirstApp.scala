package learnspark.intro

import org.apache.spark.{SparkConf, SparkContext}

/**
 * First App for wordcount
 * Created by yangjing on 15-7-23.
 */
object FirstApp {
  def main(args: Array[String]): Unit = {
    val inputFile = System.getenv("SPARK_HOME") + "/README.md"
    val outputFile = "/tmp/learnspark/firstApp"

    val conf = new SparkConf().setAppName("wordCount").setMaster("spark://192.168.1.104:7077")
    val sc = new SparkContext(conf)

    val input = sc.textFile(inputFile)
    val words = input.flatMap(_.split(' '))
    val counts = words.map((_, 1)).reduceByKey { case (x, y) => x + y }
    counts.saveAsTextFile(outputFile)
  }
}

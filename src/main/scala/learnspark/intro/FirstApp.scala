package learnspark.intro

import org.apache.spark._

/**
 * First App for wordcount
 * Created by yangjing on 15-7-23.
 */
object FirstApp {
  def main(args: Array[String]): Unit = {
    val inputFile = "/data/local/spark-1.4.1/README.md"
    val outputFile = "/tmp/learnspark/firstApp"

    val conf = new SparkConf().setAppName("WordCount").setMaster("spark://192.168.31.101:7077")
    val sc = new SparkContext(conf)

    val input = sc.textFile(inputFile)
    val words = input.flatMap(_.split(" "))
    words.collect().foreach(println)
//    val counts = words.map(word => (word, 1)).reduceByKey { case (x, y) => x + y }
//    counts.saveAsTextFile(outputFile)
  }
}

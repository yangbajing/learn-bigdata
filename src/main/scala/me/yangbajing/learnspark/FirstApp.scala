package me.yangbajing.learnspark

import org.apache.spark.{SparkConf, SparkContext}

/**
 * First App for wordcount
 * Created by yangjing on 15-7-23.
 */
object FirstApp {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("wordCount").setMaster("spark://192.168.31.101:7077")
    val sc = new SparkContext(conf)

    val input = sc.textFile(System.getenv("SPARK_HOME") + "/README.md")
    val words = input.flatMap(_.split(' '))
    val counts = words.map((_, 1)).reduceByKey { case (x, y) => x + y }
    counts.foreach(println)
  }
}

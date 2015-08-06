package com.ibm.spark.exercise.basic

import org.apache.spark.{SparkConf, SparkContext}

/**
 * 公司要统计过去一年搜索频率最高的 K 个科技关键词或词组
 * Created by Yang Jing on 2015-08-06.
 */
object TopKSearchKeyWords {
  def main(args: Array[String]) {
    if (args.length < 2) {
      println("Usage: TopKSearchKeyWords KeyWordsFile K")
      System.exit(1)
    }
    val conf = new SparkConf().setAppName("Spark Exercise: Top K Searching Key Words")
    val sc = new SparkContext(conf)
    val srcData = sc.textFile(args(0))
    val countedData = srcData.map(line => (line.toLowerCase, 1)).reduceByKey((a, b) => a + b)

    //for debug use
    //countedData.foreach(x => println(x))

    val sortedData = countedData.map { case (k, v) => (v, k) }.sortByKey(false)
    val topKData = sortedData.take(args(1).toInt).map { case (v, k) => (k, v) }

    topKData.foreach(println)
  }
}

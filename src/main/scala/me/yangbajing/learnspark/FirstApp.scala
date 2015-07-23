package me.yangbajing.learnspark

import org.apache.spark.{SparkContext, SparkConf}

/**
 * First App for wordcount
 * Created by yangjing on 15-7-23.
 */
object FirstApp {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("FirstApp").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)

    println("sc.version: " + sc.version)

    sc.stop()
  }
}

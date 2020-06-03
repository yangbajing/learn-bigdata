package com.ibm.spark.exercise.basic

import org.apache.spark.{ SparkContext, SparkConf }

/**
 * 统计一个 1000 万人口的所有人的平均年龄
 * Created by Yang Jing on 2015-08-06.
 */
object AvgAgeCalculator {
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Usage: %s datafile".format(getClass.getSimpleName))
      System.exit(1)
    }

    val conf = new SparkConf().setAppName("Spark Exercise:Average Age Calculator")
    val sc = new SparkContext(conf)
    val dataFile = sc.textFile("/data/workspace/learn-spark/data/sample_age_data.txt", 5)
    val count = dataFile.count()
    val ageData = dataFile.map(line => line.split(" ")(1))
    val totalAge = ageData.map(age => age.toInt).reduce(_ + _)

    println(s"Total Age: $totalAge, Number of People: $count")
    val avgAge = totalAge.toDouble / count
    println(s"Average Age is $avgAge")
  }
}

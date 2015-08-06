package com.ibm.spark.exercise.basic

import org.apache.spark.{SparkConf, SparkContext}

/**
 * 对某个省的人口 (1 亿) 性别还有身高进行统计，需要计算出男女人数，男性中的最高和最低身高，以及女性中的最高和最低身高。
 * 本案例中用到的源文件有以下格式, 三列分别是 ID，性别，身高 (cm)。
 * Created by Yang Jing on 2015-08-06.
 */
object PeopleInfoCalculator {
  def main(args: Array[String]) {
    if (args.length < 1) {
      println("Usage: PeopleInfoCalculator datafile")
      System.exit(1)
    }

    val conf = new SparkConf().setAppName("Spark Exercise: People Info(Gender & Height) Calculator")
    val sc = new SparkContext(conf)
    val dataFile = sc.textFile(args(0), 5)
    val maleData = dataFile.filter(line => line.contains("M")).map { line =>
      val arr = line.split(" ")
      (arr(1), arr(2).toInt)
    }
    val femaleData = dataFile.filter(line => line.contains("F")).map { line =>
      val arr = line.split(" ")
      (arr(1), arr(2).toInt)
    }

    //for debug use
    //maleData.collect().foreach { x => println(x)}
    //femaleData.collect().foreach { x => println(x)}
    val maleHeightData = maleData.map(line => line._2)
    val femaleHeightData = femaleData.map(line => line._2)

    //for debug use
    //maleHeightData.collect().foreach { x => println(x)}
    //femaleHeightData.collect().foreach { x => println(x)}
    val lowestMale = maleHeightData.sortBy(x => x, true).first()
    val lowestFemale = femaleHeightData.sortBy(x => x, true).first()

    //for debug use
    //maleHeightData.collect().sortBy(x => x).foreach { x => println(x)}
    //femaleHeightData.collect().sortBy(x => x).foreach { x => println(x)}
    val highestMale = maleHeightData.sortBy(x => x, false).first()
    val highestFemale = femaleHeightData.sortBy(x => x, false).first()

    println("Number of Male Peole:" + maleData.count())
    println("Number of Female Peole:" + femaleData.count())
    println("Lowest Male:" + lowestMale)
    println("Lowest Female:" + lowestFemale)
    println("Highest Male:" + highestMale)
    println("Highest Female:" + highestFemale)
  }
}

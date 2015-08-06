package com.ibm.spark.exercise.basic

import java.nio.file.{Files, Paths}
import java.security.SecureRandom

/**
 * 年龄信息文件
 * Created by Yang Jing on 2015-08-06.
 */
object SampleDataFileGenerator extends App {
  val writer = Files.newBufferedWriter(Paths.get("data/sample_age_data.txt"))
  val rand = new SecureRandom()
  for (i <- 1 to 10000000) {
    writer.write(i + " " + rand.nextInt(100))
    writer.newLine()
  }
  writer.close()
}

package com.ibm.spark.exercise.basic

import java.nio.file.{Paths, Files}
import java.security.SecureRandom

/**
 * 人口信息生成类源码
 * Created by Yang Jing on 2015-08-06.
 */
object PeopleInfoFileGenerator extends App {
  val rand = new SecureRandom()

  def getRandomGender = {
    val randNum = rand.nextInt(2) + 1
    if (randNum % 2 == 0) {
      "M"
    } else {
      "F"
    }
  }

  val writer = Files.newBufferedWriter(Paths.get("data/sample_people_info.txt"))

  for (i <- 1 to 100000000) {
    var height = rand.nextInt(220)
    if (height < 50) {
      height = height + 50
    }
    val gender = getRandomGender
    if (height < 100 && gender == "M")
      height = height + 100
    if (height < 100 && gender == "F")
      height = height + 50
    writer.write(i + " " + gender + " " + height)
    writer.newLine()
  }

  writer.close()
  println("People Information File generated successfully.")
}

package learnspark.intro

import org.apache.spark.{SparkContext, SparkConf}

object WordCount {
  def main(args: Array[String]): Unit = {
    println(args.length + " " + args.toList)
    if (args.length < 2) {
      println("run params: inputfile outputfile")
      System.exit(1)
    }

    val inputFile = args(0)
    val outputFile = args(1)
    val conf = new SparkConf().setAppName("wordCount")
    val sc = new SparkContext(conf)

    val input = sc.textFile(inputFile)
    val words = input.flatMap(_.split(' '))
    val counts = words.map((_, 1)).reduceByKey { case (x, y) => x + y }
    counts.saveAsTextFile(outputFile)
  }
}

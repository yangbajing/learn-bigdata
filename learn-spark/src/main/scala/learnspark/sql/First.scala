package learnspark.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * @author yangbajing <a href="mailto://yang.xunjing@qq.com">羊八井</a>
 * @date 2020-09-06 14:14
 */
object First {
  def main(args: Array[String]): Unit = {
    val sc = new SparkConf().setMaster("local[*]").setAppName("first")
    val spark = SparkSession.builder().config(sc).getOrCreate()

    val df = spark.read.format("json").load("docs/user.json.txt")
    df.show()

    spark.stop()
  }
}

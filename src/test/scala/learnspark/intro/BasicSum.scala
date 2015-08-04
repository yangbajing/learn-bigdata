package learnspark.intro

import learnspark.UnitWordSpec
import org.apache.spark._

class BasicSum extends UnitWordSpec {
  "BasicSum" should {
    "run" in {
      val sc = new SparkContext("local", "BasicMap", System.getenv("SPARK_HOME"))
      val input = sc.parallelize(List(1, 2, 3, 4))
      val result = input.fold(0)((x, y) => x + y)
      println(result)
      result shouldBe 10
    }
  }
}
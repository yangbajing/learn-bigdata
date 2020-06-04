package learnspark.intro

import java.nio.file.{ Paths, Files }

import learnspark.UnitWordSpec

class WordCountTest extends UnitWordSpec {
  "WordCountTest" should {
    "run" in {
      val inputFile = sys.env.getOrElse("SPARK_HOME", "/opt/local/spark-2.4.5-bin-hadoop-scala-2.12") + "/README.md"
      val outputFile = "/tmp/learnspark/firstApp"

      Files.deleteIfExists(Paths.get(outputFile))

      WordCount.execute(inputFile, outputFile, "spark://localhost:7077")
    }
  }
}

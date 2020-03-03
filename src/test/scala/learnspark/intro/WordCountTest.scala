package learnspark.intro

import java.nio.file.{Paths, Files}

import learnspark.UnitWordSpec

class WordCountTest extends UnitWordSpec {
  "WordCountTest" should {
    "run" in {
      val inputFile = System.getenv("SPARK_HOME") + "/README.md"
      val outputFile = "/tmp/learnspark/firstApp"

      Files.deleteIfExists(Paths.get(outputFile))

      WordCount.execute(inputFile, outputFile, "spark://localhost:7077")
    }
  }
}

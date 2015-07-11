import _root_.sbt.Keys._
import _root_.sbt._

object Build extends Build {
  override lazy val settings = super.settings :+ {
    shellPrompt := (s => Project.extract(s).currentProject.id + " > ")
  }

  lazy val learnSpark = Project("learn-spark", file("."))
    .settings(
      description := "learn-spark",
      version := "0.0.1",
      scalaVersion := "2.11.7",
      libraryDependencies ++= (
        "org.apache.spark" %% "spark-core" % "1.4.0" % "provided"
      )
    )
}


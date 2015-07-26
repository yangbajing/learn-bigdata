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
      homepage := Some(new URL("https://github.com/yangbajing/learn-spark")),
      organization := "cn.fenjoy",
      organizationHomepage := Some(new URL("http://www.yangbajing.me")),
      startYear := Some(2015),
      scalaVersion := "2.11.7",
      scalacOptions := Seq(
        "-encoding", "utf8",
        //"-Ylog-classpath",
        "-feature",
        "-unchecked",
        "-deprecation",
        "-explaintypes",
        "-Yno-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-unused"
      ),
      javacOptions := Seq(
        "-encoding", "utf8",
        "-deprecation",
        "-Xlint:unchecked",
        "-Xlint:deprecation"
      ),
      resolvers ++= Seq(
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
        "releases" at "http://oss.sonatype.org/content/repositories/releases",
        "maven.mirrorid" at "http://mirrors.ibiblio.org/pub/mirrors/maven2/",
        "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
        "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"),
      sources in(Compile, doc) := Seq.empty,
      publishArtifact in(Compile, packageDoc) := false,
      offline := true,
      libraryDependencies ++= Seq(
        "org.apache.spark" %% "spark-core" % verSpark,
//        ("org.apache.spark" %% "spark-sql" % verSpark % "provided"),
        "org.apache.hadoop" % "hadoop-client" % verHadoop,
        "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
      )
    )

  val verSpark = "1.4.1"
  val verHadoop = "2.6.0"
}


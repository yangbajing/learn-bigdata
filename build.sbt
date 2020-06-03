val scopeProvidedTest = "provided,test"
val verSpark = "2.4.5"
val verHadoop = "2.7.7"
val verFlink = "1.10.1"

ThisBuild / scalaVersion := "2.12.11"

ThisBuild / scalafmtOnCompile := true

lazy val learnBigdata = Project("learn-bigdata", file(".")).aggregate(learnSpark, learnFlink)

lazy val learnSpark = _project("learn-spark").settings(
  libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % verSpark % scopeProvidedTest,
      "org.apache.spark" %% "spark-sql" % verSpark % scopeProvidedTest,
      "org.apache.spark" %% "spark-streaming" % verSpark % scopeProvidedTest,
      //"org.apache.spark" %% "spark-streaming-kafka" % verSpark % scopeProvidedTest,
      ("org.apache.hadoop" % "hadoop-client" % verHadoop)
        .excludeAll(ExclusionRule(organization = "javax.servlet"))
        .exclude("commons-beanutils", "commons-beanutils-core"),
      ("org.mongodb.mongo-hadoop" % "mongo-hadoop-core" % "2.0.0")
        .excludeAll(ExclusionRule(organization = "javax.servlet"))
        .exclude("commons-beanutils", "commons-beanutils-core"),
      "org.postgresql" % "postgresql" % "42.2.10"))

lazy val learnFlink = _project("learn-flink").settings(
  libraryDependencies ++= Seq(
      "org.apache.flink" %% "flink-walkthrough-common" % verFlink,
      "org.apache.flink" %% "flink-streaming-scala" % verFlink,
      "org.slf4j" % "slf4j-log4j12" % "1.7.7" % Runtime,
      "log4j" % "log4j" % "1.2.17" % Runtime))

def _project(name: String) = Project(name, file(name)).settings(basicSettings: _*)

def basicSettings =
  Seq(
    description := "learn-bigdata",
    homepage := Some(new URL("https://github.com/yangbajing/learn-bigdata")),
    organization := "me.yangbajing.learn-bigdata",
    organizationHomepage := Some(new URL("http://www.yangbajing.me")),
    startYear := Some(2015),
    scalacOptions := Seq(
        "-encoding",
        "utf8",
        "-feature",
        "-unchecked",
        "-deprecation",
        "-Yno-adapted-args",
        "-Ywarn-dead-code",
        "-explaintypes"),
    javacOptions := Seq("-encoding", "utf8", "-deprecation", "-Xlint:unchecked", "-Xlint:deprecation"),
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,
    test in assembly := {},
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "servlet", _*)                        => MergeStrategy.first
      case PathList("org", "apache", "commons", "collections", _*) => MergeStrategy.first
      case x                                                       => (assemblyMergeStrategy in assembly).value.apply(x)
    },
    libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.1.1" % "test"))

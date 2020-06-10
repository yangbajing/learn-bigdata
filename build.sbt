val verSpark = "2.4.5"
val verHadoop = "2.7.7"
val verFlink = "1.10.1"
val verJackson = "2.10.1"

ThisBuild / scalaVersion := "2.12.11"

ThisBuild / scalafmtOnCompile := true

lazy val learnBigdata = Project("learn-bigdata", file(".")).aggregate(learnSpark, learnFlink, learnAvro)

lazy val learnAvro = _project("learn-avro")
  .dependsOn(learnCommon)
  .settings(
    avroSource := sourceDirectory.value / "resources",
    libraryDependencies ++= Seq(
        "org.apache.avro" % "avro" % "1.9.2",
        "ch.qos.logback" % "logback-classic" % "1.2.3" % Test.extend(Provided)))

lazy val learnSpark = _project("learn-spark")
  .dependsOn(learnCommon)
  .settings(libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % verSpark % Provided,
      "org.apache.spark" %% "spark-sql" % verSpark % Provided,
      "org.apache.spark" %% "spark-streaming" % verSpark % Provided,
      //"org.apache.spark" %% "spark-streaming-kafka" % verSpark % Provided,
      ("org.apache.hadoop" % "hadoop-client" % verHadoop)
        .excludeAll(ExclusionRule(organization = "javax.servlet"))
        .exclude("commons-beanutils", "commons-beanutils-core"),
      ("org.mongodb.mongo-hadoop" % "mongo-hadoop-core" % "2.0.0")
        .excludeAll(ExclusionRule(organization = "javax.servlet"))
        .exclude("commons-beanutils", "commons-beanutils-core"),
      "org.postgresql" % "postgresql" % "42.2.10"))

lazy val learnFlink = _project("learn-flink")
  .dependsOn(learnCommon)
  .settings(libraryDependencies ++= Seq(
      "org.apache.flink" %% "flink-streaming-scala" % verFlink % Provided,
      "org.apache.flink" %% "flink-table-api-scala-bridge" % verFlink % Provided,
      "org.apache.flink" %% "flink-table-planner" % verFlink % Provided,
      "org.apache.flink" %% "flink-walkthrough-common" % verFlink,
      "org.apache.flink" %% "flink-connector-kafka" % verFlink,
      "org.apache.flink" %% "flink-jdbc" % verFlink,
      "org.apache.flink" % "flink-json" % verFlink,
      "org.apache.flink" %% "flink-cep-scala" % verFlink,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % verJackson,
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % verJackson,
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % verJackson,
      "mysql" % "mysql-connector-java" % "8.0.20",
      "org.postgresql" % "postgresql" % "42.2.10",
      "org.slf4j" % "slf4j-log4j12" % "1.7.7" % Runtime,
      "log4j" % "log4j" % "1.2.17" % Runtime))

lazy val learnCommon = _project("learn-common")

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
    fork in run := true,
    libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.6",
        "org.scalatest" %% "scalatest" % "3.1.2" % "test"))

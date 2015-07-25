#!/bin/sh

rm -rf /tmp/learn-spark/intro-wordcount

$SPARK_HOME/bin/spark-submit \
  --class learnspark.intro.WordCount \
  ../target/scala-2.11/learn-spark_2.11-0.0.1.jar \
  $SPARK_HOME/README.md \
  /tmp/learn-spark/intro-wordcount


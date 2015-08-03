#!/bin/sh

rm -rf /tmp/learn-spark/intro-wordcount

$SPARK_HOME/bin/spark-submit \
  --verbose \
  --class learnspark.intro.WordCount \
  --master spark://192.168.1.102:7077 \
  ../target/scala-2.11/learn-spark_2.11-0.0.1.jar \
  $SPARK_HOME/README.md \
  /tmp/learn-spark/intro-wordcount


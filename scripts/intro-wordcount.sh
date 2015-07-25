#!/bin/sh

rm -rf /tmp/learn-spark/intro-wordcount

$SPARK_HOME/bin/spark-submit \
  --class learnspark.intro.WordCount \
  --master spark://192.168.31.101:7077 \
  --executor-memory 1G \
  ../target/scala-2.11/learn-spark_2.11-0.0.1.jar \
  $SPARK_HOME/README.md \
  /tmp/learn-spark/intro-wordcount


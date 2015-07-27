#!/bin/sh
SPARK_HOME="/data/local/spark-1.2.2-bin-hadoop2.4"

rm -rf /tmp/learn-spark/intro-wordcount

$SPARK_HOME/bin/spark-submit \
  --class learnspark.intro.WordCount \
  --master spark://192.168.31.101:7077 \
  --executor-memory 1G \
  ../target/scala-2.10/learn-spark_2.10-0.0.1.jar \
  $SPARK_HOME/README.md \
  /tmp/learn-spark/intro-wordcount


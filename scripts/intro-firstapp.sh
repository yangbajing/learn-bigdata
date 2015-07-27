#!/bin/sh

rm -rf /tmp/learn-spark/intro-firstapp

$SPARK_HOME/bin/spark-submit \
  --class learnspark.intro.FirstApp \
  --master spark://192.168.31.101:7077 \
  ../target/scala-2.11/learn-spark_2.11-0.0.1.jar \
  $SPARK_HOME/README.md \
  /tmp/learn-spark/intro-firstapp


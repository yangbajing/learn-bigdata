#!/bin/sh

$SPARK_HOME/bin/spark-submit \
  --class learnspark.realtime.StreamApp \
  --master spark://127.0.0.1:7077 \
  ../target/scala-2.11/learn-spark-assembly-2.4.0.jar


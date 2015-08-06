#!/bin/sh

$SPARK_HOME/bin/spark-submit \
  --class learnspark.realtime.StreamApp\
  --master spark://192.168.31.116:7077 \
  ../target/scala-2.11/learn-spark_2.11-0.0.1.jar 


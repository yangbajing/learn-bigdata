#!/bin/sh
$SPARK_HOME/bin/spark-submit \
  --class com.ibm.spark.exercise.basic.PeopleInfoCalculator \
  --master spark://192.168.31.116:7077 \
  --executor-memory 4G \
  ../target/scala-2.11/learn-spark_2.11-0.0.1.jar \
  ../data/sample_people_info.txt

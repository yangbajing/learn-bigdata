#!/bin/sh
$SPARK_HOME/bin/spark-submit \
  --class com.ibm.spark.exercise.basic.PeopleInfoCalculator \
  --master spark://127.0.0.1:7077 \
  --executor-memory 4G \
  ../target/scala-2.11/learn-spark-assembly-2.4.0.jar \
  ../data/sample_people_info.txt


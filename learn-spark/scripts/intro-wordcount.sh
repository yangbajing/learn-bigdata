#!/bin/sh

rm -rf /tmp/learn-spark/intro-wordcount

$SPARK_HOME/bin/spark-submit \
  --verbose \
  --class learnspark.intro.WordCount \
  --master spark://127.0.0.1:7077 \
  --executor-memory 1G \
  ../target/scala-2.11/learn-spark-assembly-2.4.0.jar \
  $SPARK_HOME/README.md \
  /tmp/learn-spark/intro-wordcount

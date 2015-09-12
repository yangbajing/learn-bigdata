#!/bin/sh
<<<<<<< HEAD
=======

>>>>>>> 766189e31c3518fade07cc9d169b599afb8922a9
rm -rf /tmp/learn-spark/intro-wordcount

$SPARK_HOME/bin/spark-submit \
  --verbose \
  --class learnspark.intro.WordCount \
<<<<<<< HEAD
  --master spark://192.168.31.101:7077 \
  --executor-memory 1G \
=======
  --master spark://192.168.1.102:7077 \
>>>>>>> 766189e31c3518fade07cc9d169b599afb8922a9
  ../target/scala-2.11/learn-spark_2.11-0.0.1.jar \
  $SPARK_HOME/README.md \
  /tmp/learn-spark/intro-wordcount

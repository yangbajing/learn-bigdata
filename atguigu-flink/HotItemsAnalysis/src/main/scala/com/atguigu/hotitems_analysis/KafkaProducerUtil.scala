package com.atguigu.hotitems_analysis

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved
  *
  * Project: UserBehaviorAnalysis
  * Package: com.atguigu.hotitems_analysis
  * Version: 1.0
  *
  * Created by wushengran on 2020/8/14 10:13
  */
object KafkaProducerUtil {
  def main(args: Array[String]): Unit = {
    writeToKafka("hotitems")
  }
  def writeToKafka(topic: String): Unit ={
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer")
    properties.setProperty("value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](properties)

    // 从文件读取数据，逐行写入kafka
    val bufferedSource = io.Source.fromFile("/home/yangjing/workspace/atguigu-flink/HotItemsAnalysis/src/main/resources/UserBehavior.csv")

    for( line <- bufferedSource.getLines() ){
      val record = new ProducerRecord[String, String](topic, line)
      producer.send(record)
    }

    producer.close()
  }
}

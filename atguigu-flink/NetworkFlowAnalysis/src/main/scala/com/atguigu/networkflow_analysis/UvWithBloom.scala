package com.atguigu.networkflow_analysis

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.{Trigger, TriggerResult}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import redis.clients.jedis.Jedis

import scala.util.hashing.MurmurHash3

/**
  * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved 
  *
  * Project: UserBehaviorAnalysis
  * Package: com.atguigu.networkflow_analysis
  * Version: 1.0
  *
  * Created by wushengran on 2020/8/14 16:41
  */
object UvWithBloom {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    // 从文件中读取数据
    val resource = getClass.getResource("/UserBehavior.csv")
    val inputStream: DataStream[String] = env.readTextFile(resource.getPath)

    // 转换成样例类类型并提取时间戳和watermark
    val dataStream: DataStream[UserBehavior] = inputStream
      .map(data => {
        val arr = data.split(",")
        UserBehavior(arr(0).toLong, arr(1).toLong, arr(2).toInt, arr(3), arr(4).toLong)
      })
      .assignAscendingTimestamps(_.timestamp * 1000L)

    val uvStream = dataStream
      .filter(_.behavior == "pv")
      .map( data => ("uv", data.userId) )
      .keyBy(_._1)
      .timeWindow(Time.hours(1))
        .trigger(new MyTrigger())    // 自定义触发器
      .process( new UvCountWithBloom() )

    uvStream.print()

    env.execute("uv with bloom job")
  }
}

// 触发器，每来一条数据，直接触发窗口计算并清空窗口状态
class MyTrigger() extends Trigger[(String, Long), TimeWindow]{
  override def onEventTime(time: Long, window: TimeWindow, ctx: Trigger.TriggerContext): TriggerResult = TriggerResult.CONTINUE

  override def onProcessingTime(time: Long, window: TimeWindow, ctx: Trigger.TriggerContext): TriggerResult = TriggerResult.CONTINUE

  override def clear(window: TimeWindow, ctx: Trigger.TriggerContext): Unit = {}

  override def onElement(element: (String, Long), timestamp: Long, window: TimeWindow, ctx: Trigger.TriggerContext): TriggerResult =
    TriggerResult.FIRE_AND_PURGE
}

// 自定义一个布隆过滤器，主要就是一个位图和hash函数
class Bloom(size: Long) extends Serializable{
  private val cap = size    // 默认cap应该是2的整次幂

  // hash函数
  def hash(value: String, seed: Int): Long = {
    var result = 0
    for( i <- 0 until value.length ){
      result = result * seed + value.charAt(i)
    }
    // 返回hash值，要映射到cap范围内
    (cap - 1) & result
  }
}

// 实现自定义的窗口处理函数
class UvCountWithBloom() extends ProcessWindowFunction[(String, Long), UvCount, String, TimeWindow]{
  // 定义redis连接以及布隆过滤器
  lazy val jedis = new Jedis("localhost", 6379)
  lazy val bloomFilter = new Bloom(1<<29)    // 位的个数：2^6(64) * 2^20(1M) * 2^3(8bit) ,64MB

  // 本来是收集齐所有数据、窗口触发计算的时候才会调用；现在每来一条数据都调用一次
  override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[UvCount]): Unit = {
    // 先定义redis中存储位图的key
    val storedBitMapKey = context.window.getEnd.toString

    // 另外将当前窗口的uv count值，作为状态保存到redis里，用一个叫做uvcount的hash表来保存（windowEnd，count）
    val uvCountMap = "uvcount"
    val currentKey = context.window.getEnd.toString
    var count = 0L
    // 从redis中取出当前窗口的uv count值
    if(jedis.hget(uvCountMap, currentKey) != null)
      count = jedis.hget(uvCountMap, currentKey).toLong

    // 去重：判断当前userId的hash值对应的位图位置，是否为0
    val userId = elements.last._2.toString
    // 计算hash值，就对应着位图中的偏移量
    val offset = bloomFilter.hash(userId, 61)
    // 用redis的位操作命令，取bitmap中对应位的值
    val isExist = jedis.getbit(storedBitMapKey, offset)
    if(!isExist){
      // 如果不存在，那么位图对应位置置1，并且将count值加1
      jedis.setbit(storedBitMapKey, offset, true)
      jedis.hset(uvCountMap, currentKey, (count + 1).toString)
    }
  }
}
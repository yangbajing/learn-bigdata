package com.atguigu.market_analysis

import java.sql.Timestamp
import java.util.UUID

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.util.Random

/**
  * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved 
  *
  * Project: UserBehaviorAnalysis
  * Package: com.atguigu.market_analysis
  * Version: 1.0
  *
  * Created by wushengran on 2020/8/17 10:12
  */

// 定义输入数据样例类
case class MarketUserBehavior(userId: String, behavior: String, channel: String, timestamp: Long)
// 定义输出数据样例类
case class MarketViewCount(windowStart: String, windowEnd: String, channel: String, behavior: String, count: Long)


// 自定义测试数据源
class SimulatedSource() extends RichSourceFunction[MarketUserBehavior]{
  // 是否运行的标识位
  var running = true
  // 定义用户行为和渠道的集合
  val behaviorSet: Seq[String] = Seq("view", "download", "install", "uninstall")
  val channelSet: Seq[String] = Seq("appstore", "weibo", "wechat", "tieba")
  val rand: Random = Random

  override def cancel(): Unit = running = false

  override def run(ctx: SourceFunction.SourceContext[MarketUserBehavior]): Unit = {
    // 定义一个生成数据最大的数量
    val maxCounts = Long.MaxValue
    var count = 0L

    // while循环，不停地随机产生数据
    while( running && count < maxCounts ){
      val id = UUID.randomUUID().toString
      val behavior = behaviorSet(rand.nextInt(behaviorSet.size))
      val channel = channelSet(rand.nextInt(channelSet.size))
      val ts = System.currentTimeMillis()

      ctx.collect(MarketUserBehavior(id, behavior, channel, ts))
      count += 1
      Thread.sleep(50L)
    }
  }
}

object AppMarketByChannel {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val dataStream = env.addSource( new SimulatedSource )
      .assignAscendingTimestamps(_.timestamp)

    // 开窗统计输出
    val resultStream = dataStream
      .filter(_.behavior != "uninstall")
      .keyBy(data => (data.channel, data.behavior))
      .timeWindow( Time.days(1), Time.seconds(5) )
      .process(new MarketCountByChannel())

    resultStream.print()

    env.execute("app market by channel job")

  }
}

// 自定义ProcessWindowFunction
class MarketCountByChannel() extends ProcessWindowFunction[MarketUserBehavior, MarketViewCount, (String, String), TimeWindow]{
  override def process(key: (String, String), context: Context, elements: Iterable[MarketUserBehavior], out: Collector[MarketViewCount]): Unit = {
    val start = new Timestamp(context.window.getStart).toString
    val end = new Timestamp(context.window.getEnd).toString
    val channel = key._1
    val behavior = key._2
    val count = elements.size
    out.collect(MarketViewCount(start, end, channel,behavior, count))
  }
}

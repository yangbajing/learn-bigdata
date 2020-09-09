package com.atguigu.networkflow_analysis

import org.apache.flink.api.common.functions.{AggregateFunction, MapFunction}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.util.Random

/**
  * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved 
  *
  * Project: UserBehaviorAnalysis
  * Package: com.atguigu.networkflow_analysis
  * Version: 1.0
  *
  * Created by wushengran on 2020/8/14 14:30
  */

// 定义输入数据样例类
case class UserBehavior(userId: Long, itemId: Long, categoryId: Int, behavior: String, timestamp: Long)
// 定义输出pv统计的样例类
case class PvCount(windowEnd: Long, count: Long)

object PageView {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
//    env.setParallelism(1)

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

    val pvStream = dataStream
      .filter(_.behavior == "pv")
//      .map( data => ("pv", 1L) )    // 定义一个pv字符串作为分组的dummy key
      .map( new MyMapper() )
      .keyBy( _._1 )     // 所有数据会被分到同一个组
      .timeWindow( Time.hours(1) )    // 1小时滚动窗口
      .aggregate( new PvCountAgg(), new PvCountWindowResult() )

    val totalPvStream = pvStream
      .keyBy(_.windowEnd)
      .process( new TotalPvCountResult() )

    totalPvStream.print()

    env.execute("pv job")

  }
}

// 自定义预聚合函数
class PvCountAgg() extends AggregateFunction[(String, Long), Long, Long]{
  override def add(value: (String, Long), accumulator: Long): Long = accumulator + 1

  override def createAccumulator(): Long = 0L

  override def getResult(accumulator: Long): Long = accumulator

  override def merge(a: Long, b: Long): Long = a + b
}

//自定义窗口函数
class PvCountWindowResult() extends WindowFunction[Long, PvCount, String, TimeWindow]{
  override def apply(key: String, window: TimeWindow, input: Iterable[Long], out: Collector[PvCount]): Unit = {
    out.collect(PvCount(window.getEnd, input.head))
  }
}

// 自定义mapper，随机生成分组的key
class MyMapper() extends MapFunction[UserBehavior, (String, Long)]{
  override def map(value: UserBehavior): (String, Long) = {
    ( Random.nextString(10), 1L )
  }
}

class TotalPvCountResult() extends KeyedProcessFunction[Long, PvCount, PvCount]{
  // 定义一个状态，保存当前所有count总和
  lazy val totalPvCountResultState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("total-pv", classOf[Long]))

  override def processElement(value: PvCount, ctx: KeyedProcessFunction[Long, PvCount, PvCount]#Context, out: Collector[PvCount]): Unit = {
    // 每来一个数据，将count值叠加在当前的状态上
     val currentTotalCount = totalPvCountResultState.value()
    totalPvCountResultState.update( currentTotalCount + value.count )
    // 注册一个windowEnd+1ms后触发的定时器
    ctx.timerService().registerEventTimeTimer(value.windowEnd + 1)
  }

  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[Long, PvCount, PvCount]#OnTimerContext, out: Collector[PvCount]): Unit = {
    val totalPvCount = totalPvCountResultState.value()
    out.collect(PvCount(ctx.getCurrentKey, totalPvCount))
    totalPvCountResultState.clear()
  }
}
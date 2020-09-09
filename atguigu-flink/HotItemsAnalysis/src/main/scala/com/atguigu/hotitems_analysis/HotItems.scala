package com.atguigu.hotitems_analysis

import java.sql.Timestamp
import java.util.Properties

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.state.{ ListState, ListStateDescriptor }
import org.apache.flink.api.java.tuple.{ Tuple, Tuple1 }
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.collection.mutable

/**
 * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved
 *
  * Project: UserBehaviorAnalysis
 * Package: com.atguigu.hotitems_analysis
 * Version: 1.0
 *
  * Created by wushengran on 2020/8/13 15:43
 */

// 定义输入数据样例类
case class UserBehavior(userId: Long, itemId: Long, categoryId: Int, behavior: String, timestamp: Long)

// 定义窗口聚合结果样例类
case class ItemViewCount(itemId: Long, windowEnd: Long, count: Long)

object HotItems {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime) // 定义事件时间语义

    // 从文件中读取数据，并转换成样例类，提取时间戳生成watermark
    val inputStream: DataStream[String] =
      env.readTextFile("/home/yangjing/workspace/atguigu-flink/HotItemsAnalysis/src/main/resources/UserBehavior.csv")

    // 从kafka读取数据
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", "consumer-group")
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

//    val inputStream = env.addSource( new FlinkKafkaConsumer[String]("hotitems", new SimpleStringSchema(), properties) )

    val dataStream: DataStream[UserBehavior] = inputStream
      .map(data => {
        val arr = data.split(",")
        UserBehavior(arr(0).toLong, arr(1).toLong, arr(2).toInt, arr(3), arr(4).toLong)
      })
      .assignAscendingTimestamps(_.timestamp * 1000L)

    // 得到窗口聚合结果
    val aggStream: DataStream[ItemViewCount] = dataStream
      .filter(_.behavior == "pv") // 过滤pv行为
      .keyBy("itemId") // 按照商品ID分组
      .timeWindow(Time.hours(1), Time.minutes(5)) // 设置滑动窗口进行统计
      .aggregate(new CountAgg(), new ItemViewWindowResult())

    val resultStream: DataStream[String] = aggStream
      .keyBy("windowEnd") // 按照窗口分组，收集当前窗口内的商品count数据
      .process(new TopNHotItems(5)) // 自定义处理流程

//    dataStream.print("data")
//    aggStream.print("agg")
    resultStream.print()

    env.execute("hot items")
  }
}

// 自定义预聚合函数AggregateFunction，聚合状态就是当前商品的count值
class CountAgg() extends AggregateFunction[UserBehavior, Long, Long] {
  // 每来一条数据调用一次add，count值加一
  override def add(value: UserBehavior, accumulator: Long): Long = accumulator + 1

  override def createAccumulator(): Long = 0L

  override def getResult(accumulator: Long): Long = accumulator

  override def merge(a: Long, b: Long): Long = a + b
}

// 示例：求取平均数的AggregateFunction
class AvgTs extends AggregateFunction[UserBehavior, (Long, Int), Long] {
  override def add(value: UserBehavior, accumulator: (Long, Int)): (Long, Int) =
    (accumulator._1 + value.timestamp, accumulator._2 + 1)

  override def createAccumulator(): (Long, Int) = (0L, 0)

  override def getResult(accumulator: (Long, Int)): Long = accumulator._1 / accumulator._2

  override def merge(a: (Long, Int), b: (Long, Int)): (Long, Int) =
    (a._1 + b._1, a._2 + b._2)
}

// 自定义窗口函数WindowFunction
class ItemViewWindowResult() extends WindowFunction[Long, ItemViewCount, Tuple, TimeWindow] {
  override def apply(key: Tuple, window: TimeWindow, input: Iterable[Long], out: Collector[ItemViewCount]): Unit = {
    val itemId = key.asInstanceOf[Tuple1[Long]].f0
    val windowEnd = window.getEnd
    val count = input.iterator.next()
    out.collect(ItemViewCount(itemId, windowEnd, count))
  }
}

// 自定义KeyedProcessFunction
class TopNHotItems(topSize: Int) extends KeyedProcessFunction[Tuple, ItemViewCount, String] {
  // 先定义状态：ListState
  private var itemViewCountListState: ListState[ItemViewCount] = _

  override def open(parameters: Configuration): Unit = {
    itemViewCountListState = getRuntimeContext.getListState(
      new ListStateDescriptor[ItemViewCount]("itemViewCount-list", classOf[ItemViewCount]))
  }

  override def processElement(
      value: ItemViewCount,
      ctx: KeyedProcessFunction[Tuple, ItemViewCount, String]#Context,
      out: Collector[String]): Unit = {
    // 每来一条数据，直接加入ListState
    itemViewCountListState.add(value)
    // 注册一个windowEnd + 1之后触发的定时器
    ctx.timerService().registerEventTimeTimer(value.windowEnd + 1)
  }

  // 当定时器触发，可以认为所有窗口统计结果都已到齐，可以排序输出了
  override def onTimer(
      timestamp: Long,
      ctx: KeyedProcessFunction[Tuple, ItemViewCount, String]#OnTimerContext,
      out: Collector[String]): Unit = {
    // 为了方便排序，另外定义一个ListBuffer，保存ListState里面的所有数据
//    val allItemViewCounts: ListBuffer[ItemViewCount] = ListBuffer()
//    val iter = itemViewCountListState.get().iterator()
//    while(iter.hasNext){
//      allItemViewCounts += iter.next()
//    }
implicit val ord: Ordering[ItemViewCount] = (x: ItemViewCount, y: ItemViewCount) =>  Ordering.Long.reversed().compare(x.count, y.count)

    val priorityQueue = new mutable.PriorityQueue[ItemViewCount]()(ord)
    itemViewCountListState
      .get()
      .forEach{ item =>
        if (priorityQueue.size < topSize) {
          priorityQueue += item
        } else if (priorityQueue.head.count < item.count) {
          priorityQueue.dequeue()
          priorityQueue += item
        }
        // else ignore
      }

    // 清空状态
    itemViewCountListState.clear()

    // 按照count大小排序，取前n个
//    val sortedItemViewCounts = allItemViewCounts.sortBy(_.count)(Ordering.Long.reverse).take(topSize)
    val sortedItemViewCounts = priorityQueue.dequeueAll.reverse

    // 将排名信息格式化成String，便于打印输出可视化展示
    val result: StringBuilder = new StringBuilder
    result.append("窗口结束时间：").append(new Timestamp(timestamp - 1)).append("\n")

    // 遍历结果列表中的每个ItemViewCount，输出到一行
    for (i <- sortedItemViewCounts.indices) {
      val currentItemViewCount = sortedItemViewCounts(i)
      result
        .append("NO")
        .append(i + 1)
        .append(": \t")
        .append("商品ID = ")
        .append(currentItemViewCount.itemId)
        .append("\t")
        .append("热门度 = ")
        .append(currentItemViewCount.count)
        .append("\n")
    }

    result.append("\n==================================\n\n")

    Thread.sleep(1000)
    out.collect(result.toString())
  }
}

package com.atguigu.loginfail_detect

import java.time.Instant
import java.util

import org.apache.flink.cep.PatternSelectFunction
import org.apache.flink.cep.scala.CEP
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

/**
 * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved
 *
  * Project: UserBehaviorAnalysis
 * Package: com.atguigu.loginfail_detect
 * Version: 1.0
 *
  * Created by wushengran on 2020/8/17 15:33
 */
object LoginFailWithCep {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    // 读取数据
    val resource = getClass.getResource("/LoginLog.csv")
    val inputStream = env.readTextFile(resource.getPath)

    // 转换成样例类类型，并提起时间戳和watermark
    val loginEventStream = inputStream
      .map(data => {
        val arr = data.split(",")
        LoginEvent(arr(0).toLong, arr(1), arr(2), Instant.ofEpochSecond(arr(3).toLong))
      })
      .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[LoginEvent](Time.seconds(3)) {
        override def extractTimestamp(element: LoginEvent): Long = element.timestamp.toEpochMilli
      })

    // 1. 定义匹配的模式，要求是一个登录失败事件后，紧跟另一个登录失败事件
    //        val loginFailPattern = Pattern
    //          .begin[LoginEvent]("firstFail").where(_.eventType == "fail")
    //          .next("secondFail").where(_.eventType == "fail")
    //          .next("thirdFail").where(_.eventType == "fail")
    //          .within(Time.seconds(5))
    val loginFailPattern =
      Pattern.begin[LoginEvent]("fail").where(_.eventType == "fail").times(3).consecutive().within(Time.seconds(5))

    // 2. 将模式应用到数据流上，得到一个PatternStream
    val patternStream = CEP.pattern(loginEventStream.keyBy(_.userId), loginFailPattern)

    // 3. 检出符合模式的数据流，需要调用select
    val loginFailWarningStream = patternStream.select(new LoginFailEventMatch())

    loginFailWarningStream.print()

    env.execute("login fail with cep job")
  }
}

// 实现自定义PatternSelectFunction
class LoginFailEventMatch() extends PatternSelectFunction[LoginEvent, LoginFailWarning] {
  override def select(pattern: util.Map[String, util.List[LoginEvent]]): LoginFailWarning = {
    // 当前匹配到的事件序列，就保存在Map里
    //    val firstFailEvent = pattern.get("firstFail").iterator().next()
    //    val thirdFailEvent = pattern.get("thirdFail").get(0)
    val iter = pattern.get("fail").iterator()
    val firstFailEvent = iter.next()
    val secondFailEvent = iter.next()
    val thirdFailEvent = iter.next()
    LoginFailWarning(firstFailEvent.userId, firstFailEvent.timestamp, thirdFailEvent.timestamp, "login fail")
  }
}

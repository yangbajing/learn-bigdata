/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spendreport

import java.util.Objects

import org.apache.flink.api.common.state.{ ValueState, ValueStateDescriptor }
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import org.apache.flink.walkthrough.common.entity.Alert
import org.apache.flink.walkthrough.common.entity.Transaction

/**
 * Skeleton code for implementing a fraud detector.
 */
object FraudDetector {
  val SMALL_AMOUNT: Double = 1.00
  val LARGE_AMOUNT: Double = 500.00
  val ONE_MINUTE: Long = 60 * 1000L
}

@SerialVersionUID(1L)
class FraudDetector extends KeyedProcessFunction[Long, Transaction, Alert] {
  @transient private var flagState: ValueState[java.lang.Boolean] = _
  @transient private var timerState: ValueState[java.lang.Long] = _

  @throws[Exception]
  override def open(parameters: Configuration): Unit = {
    flagState = getRuntimeContext.getState(new ValueStateDescriptor("flag", Types.BOOLEAN))
    timerState = getRuntimeContext.getState(new ValueStateDescriptor("timer-state", Types.LONG))
  }

  @throws[Exception]
  override def onTimer(
      timestamp: Long,
      ctx: KeyedProcessFunction[Long, Transaction, Alert]#OnTimerContext,
      out: Collector[Alert]): Unit = {
    timerState.clear()
    flagState.clear()
  }

  @throws[Exception]
  def processElement(
      in: Transaction,
      ctx: KeyedProcessFunction[Long, Transaction, Alert]#Context,
      out: Collector[Alert]): Unit = {
    val lastTransactionWasSmall = flagState.value()

    if (Objects.nonNull(lastTransactionWasSmall)) {
      if (in.getAmount > FraudDetector.LARGE_AMOUNT) {
        val alert = new Alert
        alert.setId(in.getAccountId)
        out.collect(alert)
      }

      cleanUp(ctx)
    }

    if (in.getAmount < FraudDetector.SMALL_AMOUNT) {
      flagState.update(true)

      val timer = ctx.timerService().currentProcessingTime() + FraudDetector.ONE_MINUTE
      ctx.timerService().registerProcessingTimeTimer(timer)
      timerState.update(timer)
    }
  }

  @throws[Exception]
  private def cleanUp(ctx: KeyedProcessFunction[Long, Transaction, Alert]#Context): Unit = {
    // delete timer
    val timer = timerState.value
    ctx.timerService.deleteProcessingTimeTimer(timer)

    // clean up all states
    timerState.clear()
    flagState.clear()
  }
}

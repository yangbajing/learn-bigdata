package spendreport

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.table.api._
import org.apache.flink.walkthrough.common.table.{ SpendReportTableSink, UnboundedTransactionTableSource }

object SpendReport {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    val tEnv = StreamTableEnvironment.create(env)

    tEnv.registerTableSource("transactions", new UnboundedTransactionTableSource)
    tEnv.registerTableSink("spend_report", new SpendReportTableSink)

//    val truncateDateToHour = new TruncateDateToHour
//    tEnv
//      .scan("transactions")
//      .select('accountId, truncateDateToHour('timestamp) as 'timestamp, 'amount)
//      .groupBy('accountId, 'timestamp)
//      .select('accountId, 'timestamp, 'amount.sum as 'total)
//      .insertInto("spend_report")

    tEnv
      .scan("transactions")
      .window(Tumble over 1.hour on 'timestamp as 'w)
      .groupBy('accountId, 'w)
      .select('accountId, 'w.start as 'timestamp, 'amount.sum)
      .insertInto("spend_report")

    env.execute("Spend Report")
  }
}

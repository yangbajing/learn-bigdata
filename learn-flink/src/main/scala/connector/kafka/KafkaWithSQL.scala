package connector.kafka

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.types.Row
import org.apache.flink.util.Collector

case class SourceKafka(name: String, age: Int, t: Long)

/**
 * @note Flink Table/SQL for Kafka 不支持设置 exactly once semantic.
 */
object KafkaWithSQL {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(1000)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    val tEnv = StreamTableEnvironment.create(env)

    tEnv.sqlUpdate("""CREATE TABLE user_action (
                     |    name STRING,
                     |    age  INT,
                     |    t    BIGINT,
                     |    WATERMARK FOR t AS t - INTERVAL '5' SECOND
                     |) WITH (
                     |  'connector.type' = 'kafka',
                     |  'connector.version' = 'universal',
                     |  'connector.topic' = 'user_action',
                     |  'connector.startup-mode' = 'earliest-offset',
                     |  'connector.properties.zookeeper.connect' = 'localhost:2181',
                     |  'connector.properties.bootstrap.servers' = 'localhost:9092',
                     |  'connector.properties.group.id' = 'testGroup',
                     |  'format.type'='json',
                     |  'update-mode' = 'append'
                     |)""".stripMargin)

//    val input = env.fromElements(
//      SourceKafka("羊八井", 34, System.currentTimeMillis()),
//      SourceKafka("杨景", 34, System.currentTimeMillis()))
//    val table = tEnv.fromDataStream(input)
//    table.insertInto("user_action")

    val table: AllWindowedStream[Row, TimeWindow] =
      tEnv.sqlQuery("""SELECT TUMBLE_START(t, INTERVAL '5' MINUTE), COUNT(DISTINCT name)
        |FROM user_action
        |GROUP BY TUMBLE(t, INTERVAL '5' MINUTE)""".stripMargin).timeWindowAll(Time.seconds(5))

    table
      .process(new ProcessAllWindowFunction[Row, Iterable[Row], TimeWindow] {
        override def process(context: Context, elements: Iterable[Row], out: Collector[Iterable[Row]]): Unit =
          out.collect(elements)
      })
      .flatMap(v => v)
      .print()

    tEnv.execute("Kafka-with-SQL")
  }
}

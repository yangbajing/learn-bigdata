package getting.sql

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.table.api._
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

class SQLTest extends AnyFunSuite with BeforeAndAfter {
  private var env: StreamExecutionEnvironment = _
  private var tEnv: StreamTableEnvironment = _

  before {
    env = StreamExecutionEnvironment.getExecutionEnvironment
    tEnv = StreamTableEnvironment.create(env)
  }

  test("explain") {
    val table1 = env.fromElements((1, "你好")).toTable(tEnv, 'count, 'word)
    val table2 = env.fromElements((1, "你好")).toTable(tEnv, 'count, 'word)
    val table = table1.where('word.like("F%")).unionAll(table2)
    val explanation = tEnv.explain(table)
    println(explanation)
  }
}

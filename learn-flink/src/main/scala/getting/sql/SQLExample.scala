package getting.sql

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.types.Row

/**
 * From MySQL read data and write to PostgreSQL.
 */
object SQLExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tEnv = StreamTableEnvironment.create(env)

    tEnv.sqlUpdate("""create table MySQLNameTest(
        |  name varchar,
        |  t timestamp,
        |  seq int
        |) with (
        |  'connector.type' = 'jdbc',
        |  'connector.url' = 'jdbc:mysql://localhost:3306/bigdata?serverTimezone=Asia/Shanghai',
        |  'connector.table' = 'name_test',
        |  'connector.driver' = 'com.mysql.cj.jdbc.Driver',
        |  'connector.username' = 'bigdata',
        |  'connector.password' = 'Bigdata.2020'
        |)""".stripMargin)

    tEnv.sqlUpdate("""create table PostgreSQLNameTest(
        |  name varchar,
        |  t timestamp,
        |  seq int
        |) with (
        |  'connector.type' = 'jdbc',
        |  'connector.url' = 'jdbc:postgresql://localhost:5432/bigdata',
        |  'connector.table' = 'name_test',
        |  'connector.driver' = 'org.postgresql.Driver',
        |  'connector.username' = 'bigdata',
        |  'connector.password' = 'Bigdata.2020'
        |)""".stripMargin)

    tEnv.sqlUpdate("""insert into PostgreSQLNameTest
        |select name, t, seq from MySQLNameTest""".stripMargin)

    val queryFromMySQL = tEnv.sqlQuery("select name, t, seq from MySQLNameTest")

    tEnv.toAppendStream[Row](queryFromMySQL).print()

//    queryFromMySQL.insertInto("PostgreSQLNameTest")

    tEnv.execute("mysql-to-postgres")
  }
}

package connector.jdbc

import java.sql.{ Connection, DriverManager, PreparedStatement }

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{ RichSinkFunction, SinkFunction }
import org.slf4j.LoggerFactory

class GenericJdbcSinkFunction[T](
    sql: String,
    statementBuilder: JdbcStatementBuilder[T],
    connectionOptions: JdbcConnectionOptions)
    extends RichSinkFunction[T] {
  private val logger = LoggerFactory.getLogger(getClass)
  private var conn: Connection = _
  private var pstmt: PreparedStatement = _

  override def invoke(value: T, context: SinkFunction.Context[_]): Unit = {
    statementBuilder.accept(pstmt, value)
    val rets = pstmt.executeBatch()
    logger.debug(s"Executing jdbc insertion returns a row count is ${java.util.Arrays.toString(rets)}.")
  }

  override def open(parameters: Configuration): Unit = {
    Class.forName(connectionOptions.getDriverName)
    conn =
      if (connectionOptions.getUsername.isPresent)
        DriverManager.getConnection(
          connectionOptions.getDbURL,
          connectionOptions.getUsername.get(),
          connectionOptions.getPassword.orElse(null))
      else DriverManager.getConnection(connectionOptions.getDbURL)
    pstmt = conn.prepareStatement(sql)
  }

  override def close(): Unit = {
    if (pstmt != null) {
      pstmt.close()
      pstmt = null
    }
    if (conn != null) {
      conn.close()
      conn = null
    }
  }
}

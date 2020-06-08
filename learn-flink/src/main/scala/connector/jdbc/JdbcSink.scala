package connector.jdbc

import org.apache.flink.streaming.api.functions.sink.SinkFunction

object JdbcSink {
  def sink[T](
      sql: String,
      statementBuilder: JdbcStatementBuilder[T],
      connectionOptions: JdbcConnectionOptions): SinkFunction[T] =
    new GenericJdbcSinkFunction[T](sql, statementBuilder, connectionOptions)
}

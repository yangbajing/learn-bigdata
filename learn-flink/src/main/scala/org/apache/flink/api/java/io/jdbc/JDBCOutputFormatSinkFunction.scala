package org.apache.flink.api.java.io.jdbc

import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{ FunctionInitializationContext, FunctionSnapshotContext }
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.functions.sink.SinkFunction.Context
import org.apache.flink.types.Row

class JDBCOutputFormatSinkFunction(outputFormat: JDBCOutputFormat)
    extends RichSinkFunction[Row]
    with CheckpointedFunction {
//  private val logger = LoggerFactory.getLogger(getClass)

  @throws[Exception]
  override def invoke(value: Row, context: Context[_]): Unit = outputFormat.writeRecord(value)

  @throws[Exception]
  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    val ctx = getRuntimeContext
    outputFormat.setRuntimeContext(ctx)
    outputFormat.open(ctx.getIndexOfThisSubtask, ctx.getNumberOfParallelSubtasks)
  }

  @throws[Exception]
  override def close(): Unit = {
    outputFormat.close()
    super.close()
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {
//    logger.debug(s"Beginning snapshot, current context is $context.")
    outputFormat.flush()
  }

  override def initializeState(context: FunctionInitializationContext): Unit = {}
}

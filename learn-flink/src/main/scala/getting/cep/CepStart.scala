package getting.cep

import org.apache.flink.cep.functions.PatternProcessFunction
import org.apache.flink.cep.scala._
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.flink.walkthrough.common.entity.Alert

import scala.beans.BeanProperty

trait Event {
  def getId: Int
  def getVolume: Double
  def getName: String
}

case class SubEvent(@BeanProperty id: Int, @BeanProperty name: String, @BeanProperty volume: Double) extends Event

object CepStart {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val input: DataStream[Event] = env.fromElements(SubEvent(42, "name", 0.0))

    val pattern = Pattern
      .begin[Event]("start")
      .where(_.getId == 42)
      .next("middle")
      .subtype(classOf[SubEvent])
      .where(_.getVolume >= 10.0)
      .followedBy("end")
      .where(_.getName == "end")

    val patternStream: PatternStream[Event] = CEP.pattern(input, pattern)

    val result = patternStream.process(new PatternProcessFunction[Event, Alert]() {
      def createAlertFrom(pattern: Pattern[Event, Event]): Alert = {
        ???
      }

      override def processMatch(
          `match`: java.util.Map[String, java.util.List[Event]],
          ctx: PatternProcessFunction.Context,
          out: Collector[Alert]): Unit = {
        out.collect(createAlertFrom(pattern))
      }
    })

    result.print()

    env.execute("CEP start.")
  }
}

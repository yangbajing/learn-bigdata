package learn.avro.util

import org.apache.avro.Schema
import org.apache.avro.Schema.Type
import scala.jdk.CollectionConverters._

final class CheckOption(f: Schema.Field, datum: Object) {
  private val props = f.getObjectProps

  @inline def require(requirement: Boolean, message: String) {
    if (!requirement)
      throw new IllegalArgumentException(message)
  }

  def check(): Unit = {
    check(f.schema().getType)
  }

  def check(typ: Type): Unit = {
    typ match {
      case Type.STRING =>
        val v = datum.asInstanceOf[String]
        checkMax(v)
        checkMin(v)
      case Type.INT =>
        val v = datum.asInstanceOf[Integer]
        checkMin(v)
        checkMax(v)
      case Type.LONG =>
        val v = datum.asInstanceOf[java.lang.Long]
        checkMin(v)
        checkMax(v)
      case Type.FLOAT =>
        val v = datum.asInstanceOf[java.lang.Float]
        checkMin(v)
        checkMax(v)
      case Type.DOUBLE =>
        val v = datum.asInstanceOf[java.lang.Double]
        checkMin(v)
        checkMax(v)
      case Type.UNION =>
        val types = f.schema().getTypes.asScala.filterNot(_.getType == Type.NULL)
        if (types.nonEmpty) {
          check(types.map(_.getType).head)
        }
      case _ =>
    }
  }

  private def checkMin(v: String): Unit = {
    props.get("min") match {
      case min: Integer => require(v.length >= min, s"String length must be >= $min, received [${f.name()}] is '$v'.")
      case _            => // do nothing
    }
  }

  private def checkMax(v: String): Unit = {
    props.get("max") match {
      case max: Integer => require(v.length <= max, s"String length must be <= $max, received [${f.name()}] is '$v'.")
      case _            => // do nothing
    }
  }

  private def checkMax[T <: Comparable[T]](v: T): Unit = {
    val obj = props.get("max")
    if (obj != null) {
      val max = obj.asInstanceOf[Comparable[T]]
      require(max.compareTo(v) >= 0, s"${max.getClass.getSimpleName} must be <= $max, received [${f.name()}] is $v.")
    }
  }

  private def checkMin[T <: Comparable[T]](v: T): Unit = {
    val obj = props.get("min")
    if (obj != null) {
      val min = obj.asInstanceOf[Comparable[T]]
      require(min.compareTo(v) <= 0, s"${min.getClass.getSimpleName} must be >= $min, received [${f.name()}] is $v.")
    }
  }
}

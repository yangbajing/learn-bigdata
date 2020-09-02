package learn.avro

import org.apache.avro.Schema
import org.apache.avro.Schema.Type

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

final class CheckError(val message: String, val field: Schema.Field, val datum: Object) {
  override def toString = s"CheckError($message, $field, $datum)"
}

final class CheckErrorCollector(collectAllError: Boolean) {
  private[avro] var errors = List[CheckError]()

  def check(f: Schema.Field, datum: Object): Unit = {
    try {
      new CheckOption(f, datum).check()
    } catch {
      case e: RuntimeException =>
        if (collectAllError) errors ::= new CheckError(e.getMessage, f, datum) else throw e
    }
  }

  private class CheckOption(f: Schema.Field, datum: Object) {
    private val props = f.getObjectProps

    @inline
    def check(): Unit = check(f.schema().getType)

    @tailrec
    final def check(typ: Type): Unit = {
      typ match {
        case Type.STRING =>
          val v = datum.asInstanceOf[String]
          checkSize("size", obj => {
            val size = obj.asInstanceOf[Integer]
            if (v.length == size) None
            else Some(s"String length must be $size, received [${f.name()}] is $v.")
          })
          checkMin(v, v.length >= _.intValue())
          checkMax(v, v.length <= _.intValue())
        case Type.INT =>
          val v = datum.asInstanceOf[Integer]
          checkMin(v, v.intValue() >= _.intValue())
          checkMax(v, v.intValue() <= _.intValue())
        case Type.LONG =>
          val v = datum.asInstanceOf[java.lang.Long]
          checkMin(v, v.longValue() >= _.longValue())
          checkMax(v, v.longValue() <= _.longValue())
        case Type.FLOAT =>
          val v = datum.asInstanceOf[java.lang.Float]
          checkMin(v, v.floatValue() >= _.floatValue())
          checkMax(v, v.floatValue() <= _.floatValue())
        case Type.DOUBLE =>
          val v = datum.asInstanceOf[java.lang.Double]
          checkMin(v, v.doubleValue() >= _.doubleValue())
          checkMax(v, v.doubleValue() <= _.doubleValue())
        case Type.UNION =>
          val types = f.schema().getTypes.asScala.filterNot(v => v.getType == Type.NULL || v.getType == Type.RECORD)
          if (types.nonEmpty) {
            check(types.map(_.getType).head)
          }
        case _ =>
      }
    }

    @inline private def require(message: String): Unit = {
      throw new IllegalArgumentException(message)
    }

    private def checkSize[T](propKey: String, predicate: Object => Option[String]): Unit = {
      val obj = props.get(propKey)
      if (obj != null) {
        predicate(obj) match {
          case Some(message) => require(message)
          case _             => // do nothing
        }
      }
    }

    private def checkMax[T](v: T, predicate: Number => Boolean): Unit = {
      checkSize("max", obj => {
        val max = obj.asInstanceOf[Number]
        if (predicate(max)) None
        else Some(s"${v.getClass.getSimpleName} must be <= $max, received [${f.name()}] is $v.")
      })
    }

    private def checkMin[T](v: T, predicate: Number => Boolean): Unit = {
      checkSize("min", obj => {
        val min = obj.asInstanceOf[Number]
        if (predicate(min)) None
        else Some(s"${v.getClass.getSimpleName} must be >= $min, received [${f.name()}] is $v.")
      })
    }
  }
}

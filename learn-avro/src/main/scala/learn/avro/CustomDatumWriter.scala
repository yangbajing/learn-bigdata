package learn.avro

import java.io.IOException

import org.apache.avro.Schema
import org.apache.avro.Schema.Type
import org.apache.avro.generic.{ GenericData, GenericDatumWriter }
import org.apache.avro.io.Encoder

import scala.collection.immutable

class CustomDatumWriter[T](root: Schema, data: GenericData, collectAllError: Boolean)
    extends GenericDatumWriter[T](root, data) {
  private val checkCollector = new CheckErrorCollector(collectAllError)
  private var curField: Schema.Field = _

  def this() {
    this(null, GenericData.get, false)
  }

  def this(data: GenericData) {
    this(null, data, false)
  }

  def this(root: Schema) {
    this(root, GenericData.get, false)
  }

  def errors: immutable.Seq[CheckError] = checkCollector.errors

  @throws[IOException]
  override def writeField(datum: Any, f: Schema.Field, out: Encoder, state: Any): Unit = {
    curField = f
    super.writeField(datum, f, out, state)
  }

  @throws[IOException]
  override def writeWithoutConversion(schema: Schema, datum: Object, out: Encoder): Unit = {
    if (curField != null) {
      schema.getType match {
        case Type.STRING | Type.INT | Type.LONG | Type.FLOAT | Type.DOUBLE => checkCollector.check(curField, datum)
        case _                                                             => // do nothing
      }
    }
    super.writeWithoutConversion(schema, datum, out)
  }
}

package learn.avro

import java.io.IOException

import learn.avro.util.CheckOption
import org.apache.avro.Schema
import org.apache.avro.Schema.Type
import org.apache.avro.generic.{ GenericData, GenericDatumWriter }
import org.apache.avro.io.Encoder

class CustomDatumWriter[T](root: Schema, data: GenericData) extends GenericDatumWriter[T](root, data) {
  private var curField: Schema.Field = _
  def this() {
    this(null, GenericData.get)
  }

  def this(data: GenericData) {
    this(null, data)
  }

  def this(root: Schema) {
    this(root, GenericData.get)
  }

  @throws[IOException]
  override def writeField(datum: Any, f: Schema.Field, out: Encoder, state: Any): Unit = {
    curField = f
    super.writeField(datum, f, out, state)
  }

  @throws[IOException]
  override def writeWithoutConversion(schema: Schema, datum: Object, out: Encoder): Unit = {
    if (curField != null) {
      schema.getType match {
        case Type.STRING | Type.INT | Type.LONG | Type.FLOAT | Type.DOUBLE => new CheckOption(curField, datum).check()
        case _                                                             => // do nothing
      }
    }
    super.writeWithoutConversion(schema, datum, out)
  }
}

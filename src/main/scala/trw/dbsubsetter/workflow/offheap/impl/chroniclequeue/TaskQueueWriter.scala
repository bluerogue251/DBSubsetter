package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import java.math.BigInteger
import java.util.UUID

import net.openhft.chronicle.wire.{ValueOut, WireOut, WriteMarshallable}
import trw.dbsubsetter.db.{Column, ColumnTypes}

private[offheap] final class TaskQueueWriter(fkOrdinal: Short, columns: Seq[Column]) {
  def writeHandler(fetchChildren: Boolean, fkValue: Any): WriteMarshallable = {
    wireOut => {
      val out = wireOut.getValueOut
      out.bool(fetchChildren)
      out.int16(fkOrdinal)
      valueWriter(out, fkValue)
    }
  }

  private val valueWriter: (ValueOut, Any) => Unit = {
    val funcs: Seq[(ValueOut, Any) => WireOut] =
      columns.map(_.dataType).map {
        case ColumnTypes.Short =>
          (out: ValueOut, fkVal: Any) => out.int16(fkVal.asInstanceOf[Short])
        case ColumnTypes.Int =>
          (out: ValueOut, fkVal: Any) => out.int32(fkVal.asInstanceOf[Int])
        case ColumnTypes.Long =>
          (out: ValueOut, fkVal: Any) => out.int64(fkVal.asInstanceOf[Long])
        case ColumnTypes.BigInteger =>
          (out: ValueOut, fkVal: Any) => out.`object`(fkVal.asInstanceOf[BigInteger])
        case ColumnTypes.String =>
          (out: ValueOut, fkVal: Any) => out.text(fkVal.asInstanceOf[String])
        case ColumnTypes.ByteArray =>
          (out: ValueOut, fkVal: Any) => out.bytes(fkVal.asInstanceOf[Array[Byte]])
        case ColumnTypes.Uuid =>
          (out: ValueOut, fkVal: Any) => out.uuid(fkVal.asInstanceOf[UUID])
        case dataType =>
          val errorMessage =
            s"Type not yet supported for serialized storage: $dataType. " +
            "Please open a GitHub issue and we will try to address it promptly."
          throw new RuntimeException(errorMessage)
      }

    if (columns.size == 1) {
      (out, fkValue) => funcs.head(out, fkValue)
    } else {
      (out, fkValues) => fkValues.asInstanceOf[Array[Any]].zip(funcs).foreach { case (v, f) => f(out, v) }
    }
  }
}

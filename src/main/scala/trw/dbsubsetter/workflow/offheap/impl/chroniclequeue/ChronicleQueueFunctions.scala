package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import java.math.BigInteger
import java.util.UUID

import net.openhft.chronicle.wire.{ValueIn, ValueOut, WireOut}
import trw.dbsubsetter.db.ColumnTypes
import trw.dbsubsetter.db.ColumnTypes.ColumnType

object ChronicleQueueFunctions {

  def singleValueRead(dataType: ColumnType): ValueIn => Any = {
    dataType match {
      case ColumnTypes.Short =>
        (in: ValueIn) => in.int16()
      case ColumnTypes.Int =>
        (in: ValueIn) => in.int32()
      case ColumnTypes.Long =>
        (in: ValueIn) => in.int64()
      case ColumnTypes.BigInteger =>
        (in: ValueIn) => in.`object`()
      case ColumnTypes.String =>
        (in: ValueIn) => in.text()
      case ColumnTypes.ByteArray =>
        (in: ValueIn) => in.bytes()
      case ColumnTypes.Uuid =>
        (in: ValueIn) => in.uuid()
      case ColumnTypes.Unknown(description) =>
        val errorMessage =
          s"Column type not yet fully supported: $description. " +
            "Please open a GitHub issue and we will try to address it promptly."
        throw new RuntimeException(errorMessage)
    }
  }

  def singleValueWrite(dataType: ColumnType): (ValueOut, Any) => WireOut = {
    dataType match {
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
      case ColumnTypes.Unknown(description) =>
        val errorMessage =
          s"Column type not yet fully supported: $description. " +
            "Please open a GitHub issue and we will try to address it promptly."
        throw new RuntimeException(errorMessage)
    }
  }
}

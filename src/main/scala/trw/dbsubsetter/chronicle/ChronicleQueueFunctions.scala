package trw.dbsubsetter.chronicle

import net.openhft.chronicle.wire.{ValueIn, ValueOut, WireOut}
import trw.dbsubsetter.db.ColumnTypes
import trw.dbsubsetter.db.ColumnTypes._

object ChronicleQueueFunctions {

  def singleValueRead(dataType: ColumnType): ValueIn => Any = {
    dataType match {
      case Short =>
        (in: ValueIn) => in.int16()
      case Int =>
        (in: ValueIn) => in.int32()
      case Long =>
        (in: ValueIn) => in.int64()
      case ColumnTypes.BigInteger =>
        (in: ValueIn) => in.`object`()
      case String =>
        (in: ValueIn) => in.text()
      case ByteArray =>
        (in: ValueIn) => in.bytes()
      case Uuid =>
        (in: ValueIn) => in.uuid()
      case Unknown(description) =>
        val errorMessage =
          s"Column type not yet fully supported: $description. " +
            "Please open a GitHub issue and we will try to address it promptly."
        throw new RuntimeException(errorMessage)
    }
  }

  def singleValueWrite(dataType: ColumnType): (ValueOut, Array[Byte]) => WireOut = {
    dataType match {
      case Short | Int | Long | Uuid =>
        (out: ValueOut, fkVal: Any) => {
          val bytes: Array[Byte] = fkVal.asInstanceOf[Array[Byte]]
          out.rawBytes(bytes)
        }
      case ColumnTypes.BigInteger | String | ByteArray =>
        (out: ValueOut, fkVal: Any) => {
          val bytes: Array[Byte] = fkVal.asInstanceOf[Array[Byte]]
          out.writeInt(bytes.length)
          out.rawBytes(bytes)
        }
      case Unknown(description) =>
        val errorMessage =
          s"Column type not yet fully supported: $description. " +
            "Please open a GitHub issue and we will try to address it promptly."
        throw new RuntimeException(errorMessage)
    }
  }
}

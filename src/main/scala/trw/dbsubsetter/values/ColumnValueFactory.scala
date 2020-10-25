package trw.dbsubsetter.values

import java.nio.ByteBuffer

import trw.dbsubsetter.db.ColumnTypes
import trw.dbsubsetter.db.ColumnTypes.ColumnType

object ColumnValueFactory {
  def build(columnType: ColumnType, value: Any): Unit = {
    if (value == null) {
      NullColumnValue
    } else {
      columnType match {
        case ColumnTypes.Short =>
          val short: Short = value.asInstanceOf[Short]
          val bytes: Array[Byte] = ByteBuffer.allocate(2).putShort(short).array()
          ShortColumnValue(bytes)
        case ColumnTypes.Int =>
          val int: Int = value.asInstanceOf[Int]
          val bytes: Array[Byte] = ByteBuffer.allocate(4).putInt(int).array()
          IntColumnValue(bytes)
        case ColumnTypes.Long =>
          val long: Long = value.asInstanceOf[Long]
          val bytes: Array[Byte] = ByteBuffer.allocate(8).putLong(long).array()
          IntColumnValue(bytes)
        case ColumnTypes.BigInteger =>

        case ColumnTypes.String               =>
        case ColumnTypes.ByteArray            =>
        case ColumnTypes.Uuid                 =>
        case ColumnTypes.Unknown(description) =>
      }
    }
  }
}

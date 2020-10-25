package trw.dbsubsetter.db.impl.origin

import java.nio.ByteBuffer
import java.util.UUID

import trw.dbsubsetter.db.ColumnTypes
import trw.dbsubsetter.db.ColumnTypes.ColumnType

object WootConverter {
  def convert(bytes: Array[Byte], columnType: ColumnType): Any = {
    lazy val buffer: ByteBuffer = ByteBuffer.wrap(bytes)
    columnType match {
      case ColumnTypes.Short                => buffer.getShort
      case ColumnTypes.Int                  => buffer.getInt()
      case ColumnTypes.Long                 => buffer.getLong()
      case ColumnTypes.BigInteger           => BigInt.apply(bytes)
      case ColumnTypes.String               => new String(bytes)
      case ColumnTypes.ByteArray            => bytes
      case ColumnTypes.Uuid                 => new UUID(buffer.getLong(), buffer.getLong())
      case ColumnTypes.Unknown(description) => throw new IllegalArgumentException
    }
  }
}

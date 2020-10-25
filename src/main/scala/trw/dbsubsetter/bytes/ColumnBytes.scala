package trw.dbsubsetter.bytes

import java.nio.ByteBuffer

import trw.dbsubsetter.values._

object ColumnBytes {
  def toBytes(columnValue: ColumnValue): Array[Byte] = {
    columnValue match {
      case NullColumnValue =>
        throw new IllegalStateException("Null value unsupported")

      case ShortColumnValue(short) =>
        ByteBuffer.allocate(2).putShort(short).array()

      case IntColumnValue(int) =>
        ByteBuffer.allocate(4).putInt(int).array()

      case LongColumnValue(long) =>
        ByteBuffer.allocate(8).putLong(long).array()

      case BigIntColumnValue(bigInt) =>
        val valueBytes: Array[Byte] = bigInt.toByteArray
        ByteBuffer
          .allocate(4 + valueBytes.length)
          .putInt(valueBytes.length)
          .put(valueBytes)
          .array()

      case StringColumnValue(string) =>
        val stringBytes: Array[Byte] = string.getBytes
        ByteBuffer
          .allocate(4 + stringBytes.length)
          .putInt(stringBytes.length)
          .put(stringBytes)
          .array()

      case UUIDColumnValue(uuid) =>
        ByteBuffer
          .allocate(16)
          .putLong(uuid.getMostSignificantBits)
          .putLong(uuid.getLeastSignificantBits)
          .array()

      case ByteArrayColumnValue(bytes) =>
        ByteBuffer
          .allocate(4 + bytes.length)
          .putInt(bytes.length)
          .put(bytes)
          .array()
    }
  }
}

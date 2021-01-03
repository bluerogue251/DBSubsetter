package trw.dbsubsetter.db.value

import java.nio.ByteBuffer
import java.util.UUID

trait ColumnValue {
  def bytes: Array[Byte]
}

private[this] class IntColumnValue(x: Int) extends ColumnValue {
  override def bytes: Array[Byte] =
    ByteBuffer.allocate(4).putInt(x).array()
}

private[this] class LongColumnValue(x: Long) extends ColumnValue {
  override def bytes: Array[Byte] =
    ByteBuffer.allocate(8).putLong(x).array()
}

private[this] class BigIntColumnValue(x: BigInt) extends ColumnValue {
  override def bytes: Array[Byte] = x.toByteArray
}

private[this] class StringColumnValue(x: String) extends ColumnValue {
  override def bytes: Array[Byte] = x.getBytes
}

private[this] class UUIDColumnValue(x: UUID) extends ColumnValue {
  override def bytes: Array[Byte] =
    ByteBuffer
      .allocate(16)
      .putLong(x.getMostSignificantBits)
      .putLong(x.getLeastSignificantBits)
      .array()
}

private[this] class BytesColumnValue(val x: Array[Byte]) extends ColumnValue {
  override def bytes: Array[Byte] = x
}

private[this] class ObjectColumnValue(val x: AnyRef) extends ColumnValue {
  override def bytes: Array[Byte] = throw new RuntimeException("Can't serialize generic object type column.")
}

object ColumnValue {
  def int(x: Int): ColumnValue = new IntColumnValue(x)
  def long(x: Long): ColumnValue = new LongColumnValue(x)
  def bigInt(x: BigInt): ColumnValue = new BigIntColumnValue(x)
  def string(x: String): ColumnValue = new StringColumnValue(x)
  def uuid(x: UUID): ColumnValue = new UUIDColumnValue(x)
  def bytes(x: Array[Byte]): ColumnValue = new BytesColumnValue(x)
  def unknown(x: AnyRef): ColumnValue = new ObjectColumnValue(x)
}

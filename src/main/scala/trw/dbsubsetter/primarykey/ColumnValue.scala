package trw.dbsubsetter.primarykey

import java.nio.ByteBuffer
import java.util.UUID

trait ColumnValue {
  def bytes: Array[Byte]
}

private[this] class IntValue(x: Int) extends ColumnValue {
  override def bytes: Array[Byte] =
    ByteBuffer.allocate(4).putInt(x).array()
}

private[this] class LongValue(x: Long) extends ColumnValue {
  override def bytes: Array[Byte] =
    ByteBuffer.allocate(8).putLong(x).array()
}

private[this] class BigIntValue(x: BigInt) extends ColumnValue {
  override def bytes: Array[Byte] = x.toByteArray
}

private[this] class StringValue(x: String) extends ColumnValue {
  override def bytes: Array[Byte] = x.getBytes
}

private[this] class UUIDValue(x: UUID) extends ColumnValue {
  override def bytes: Array[Byte] =
    ByteBuffer
      .allocate(16)
      .putLong(x.getMostSignificantBits)
      .putLong(x.getLeastSignificantBits)
      .array()
}

private[this] class ByteArrayValue(x: Array[Byte]) extends ColumnValue {
  override def bytes: Array[Byte] = x
}

private[this] class CompositeValue(members: Seq[ColumnValue]) extends ColumnValue {
  override def bytes: Array[Byte] = {
    val totalSize =
      (members.size * 4) +
        members
          .map(_.bytes)
          .map(_.length)
          .sum

    val buffer: ByteBuffer = ByteBuffer.allocate(totalSize)
    members.foreach(member => buffer.putInt(member.bytes.length).put(member.bytes))
    buffer.array()
  }
}

object ColumnValue {
  def apply(x: Int): ColumnValue = new IntValue(x)
  def apply(x: Long): ColumnValue = new LongValue(x)
  def apply(x: BigInt): ColumnValue = new BigIntValue(x)
  def apply(x: String): ColumnValue = new StringValue(x)
  def apply(x: UUID): ColumnValue = new UUIDValue(x)
  def apply(x: Array[Byte]): ColumnValue = new ByteArrayValue(x)
  def apply(x: Seq[ColumnValue]): ColumnValue = new CompositeValue(x)
}

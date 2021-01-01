package trw.dbsubsetter.value

import java.nio.ByteBuffer
import java.util.UUID

trait Value {
  def bytes: Array[Byte]
}

private[this] class IntValue(x: Int) extends Value {
  override def bytes: Array[Byte] =
    ByteBuffer.allocate(4).putInt(x).array()
}

private[this] class LongValue(x: Long) extends Value {
  override def bytes: Array[Byte] =
    ByteBuffer.allocate(8).putLong(x).array()
}

private[this] class BigIntValue(x: BigInt) extends Value {
  override def bytes: Array[Byte] = x.toByteArray
}

private[this] class StringValue(x: String) extends Value {
  override def bytes: Array[Byte] = x.getBytes
}

private[this] class UUIDValue(x: UUID) extends Value {
  override def bytes: Array[Byte] =
    ByteBuffer
      .allocate(16)
      .putLong(x.getMostSignificantBits)
      .putLong(x.getLeastSignificantBits)
      .array()
}

private[this] class ByteArrayValue(x: Array[Byte]) extends Value {
  override def bytes: Array[Byte] = x
}

private[this] class CompositeValue(members: Seq[Value]) extends Value {
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

object Value {
  def apply(x: Int): Value = new IntValue(x)
  def apply(x: Long): Value = new LongValue(x)
  def apply(x: BigInt): Value = new BigIntValue(x)
  def apply(x: String): Value = new StringValue(x)
  def apply(x: UUID): Value = new UUIDValue(x)
  def apply(x: Array[Byte]): Value = new ByteArrayValue(x)
  def apply(x: Seq[Value]): Value =
    if (x.size == 1)
      x.head
    else
      new CompositeValue(x)
}

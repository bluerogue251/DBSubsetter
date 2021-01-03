package trw.dbsubsetter.db.value

import java.nio.ByteBuffer

trait KeyValue {
  def bytes: Array[Byte]
}

private[this] class SingleColumnKeyValue(x: ColumnValue) extends KeyValue {
  override def bytes: Array[Byte] = x.bytes
}

private[this] class MultiColumnKeyValue(members: Seq[ColumnValue]) extends KeyValue {
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

object KeyValue {
  def apply(members: Seq[ColumnValue]): KeyValue = {
    if (members.size == 1) {
      new SingleColumnKeyValue(members.head)
    } else {
      new MultiColumnKeyValue(members)
    }
  }
}

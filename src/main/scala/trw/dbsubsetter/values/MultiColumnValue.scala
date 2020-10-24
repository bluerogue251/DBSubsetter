package trw.dbsubsetter.values

import java.nio.ByteBuffer

final class MultiColumnValue(members: Seq[ColumnValue]) extends Value {
  override def asBytes: Array[Byte] = {
    val memberCount: Byte = members.size.toByte
    val memberSizes: Seq[Int] = members.map(_.asBytes().length)
    /*
     * 1 byte for the memberCount
     * 4 bytes for each member size
     * And enough bytes for the member contents
     */
    val capacity: Int = 2 + (4 * memberSizes.size) + memberSizes.sum
    val buffer: ByteBuffer = ByteBuffer.allocate(capacity)
    buffer.putShort(memberCount)
    memberSizes.foreach(buffer.putInt)
    members.map(member => buffer.put(member.asBytes()))
    buffer.array()
  }
}

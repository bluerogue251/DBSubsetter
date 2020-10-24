package trw.dbsubsetter.values

import java.nio.ByteBuffer

final class ShortColumnValue(value: Short) extends ColumnValue {
  override def asBytes(): Array[Byte] = {
    ByteBuffer.allocate(2).putInt(value).array()
  }
}

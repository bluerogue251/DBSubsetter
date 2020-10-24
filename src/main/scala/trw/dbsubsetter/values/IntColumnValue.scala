package trw.dbsubsetter.values

import java.nio.ByteBuffer

final class IntColumnValue(value: Int) extends ColumnValue {
  override def asBytes(): Array[Byte] = {
    ByteBuffer.allocate(4).putInt(value).array()
  }
}

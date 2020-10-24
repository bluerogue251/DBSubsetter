package trw.dbsubsetter.values

import java.nio.ByteBuffer

final class BigIntColumnValue(value: BigInt) extends ColumnValue {
  override def asBytes(): Array[Byte] = {
    ByteBuffer.allocate(8).putLong(value).array()
  }
}

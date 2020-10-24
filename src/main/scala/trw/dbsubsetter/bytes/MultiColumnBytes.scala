package trw.dbsubsetter.bytes

import java.nio.ByteBuffer

import trw.dbsubsetter.values._

object MultiColumnBytes {
  def toBytes(columnValues: Seq[ColumnValue]): Array[Byte] = {
    val columnCount: Byte = columnValues.size.toByte
    val columnBytes: Seq[Array[Byte]] = columnValues.map(ColumnBytes.toBytes)
    val columnByteSizes: Seq[Int] = columnBytes.map(_.length)

    /*
     * 1 byte to store the column count
     * 4 bytes per column to store the size of the column
     * Bytes to store each value
     */
    val capacity: Int = 1 + (4 * columnByteSizes.size) + columnByteSizes.sum

    val buffer: ByteBuffer = ByteBuffer.allocate(capacity)
    buffer.put(columnCount)
    columnByteSizes.foreach(buffer.putInt)
    columnBytes.foreach(buffer.put)
    buffer.array()
  }
}

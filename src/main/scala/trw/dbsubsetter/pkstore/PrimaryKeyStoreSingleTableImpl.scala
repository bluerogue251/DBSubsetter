package trw.dbsubsetter.pkstore

import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import trw.dbsubsetter.db.PrimaryKeyValue

private[pkstore] final class PrimaryKeyStoreSingleTableImpl extends PrimaryKeyStoreSingleTable {

  /*
   * If `storage(pkValue) == null`, then neither its parents nor its children have been fetched.
   * If `storage(pkValue) == false`, then only its parents have been fetched.
   * If `storage(pkValue) == true`, then both its children and its parents have been fetched.
   * There is no such thing as having fetched a row's children but not having fetched its parents.
   */
  private[this] val storage: ConcurrentHashMap[ByteBuffer, java.lang.Boolean] = new ConcurrentHashMap()

  override def markSeen(value: PrimaryKeyValue): WriteOutcome = {
    val valueBytes: ByteBuffer = extract(value)
    val prev: java.lang.Boolean = storage.putIfAbsent(valueBytes, false)
    interpret(prev)
  }

  override def markSeenWithChildren(value: PrimaryKeyValue): WriteOutcome = {
    val valueBytes: ByteBuffer = extract(value)
    val prev: java.lang.Boolean = storage.put(valueBytes, true)
    interpret(prev)
  }

  override def alreadySeen(value: PrimaryKeyValue): Boolean = {
    val valueBytes: ByteBuffer = extract(value)
    storage.containsKey(valueBytes)
  }

  private def extract(value: PrimaryKeyValue): ByteBuffer = {
    if (value.individualColumnValues.size == 1) {
      extractSingle(value.individualColumnValues.head)
    } else {
      val buffers: Seq[ByteBuffer] = value.individualColumnValues.map(extractSingle)
      val count: Int = buffers.size
      val bufferSizes: Seq[Int] = buffers.map(_.capacity())
      /*
       * 4 Bytes for the count
       * 4 Bytes per buffer for its size
       * Rest Bytes for all the contents
       */
      val capacity: Int = 4 + (4 * count) + bufferSizes.sum
      val compositeBuffer: ByteBuffer = ByteBuffer.allocate(capacity)
      compositeBuffer.putInt(count)
      bufferSizes.foreach(compositeBuffer.putInt)
      buffers.foreach(compositeBuffer.put)
      compositeBuffer.rewind()
      compositeBuffer
    }
  }

  private def extractSingle(value: Any): ByteBuffer = {
    val buffer: ByteBuffer = {
      value match {
        case short: Short       => ByteBuffer.allocate(2).putShort(short)
        case int: Int           => ByteBuffer.allocate(4).putInt(int)
        case long: Long         => ByteBuffer.allocate(8).putLong(long)
        case bigInt: BigInt     => ByteBuffer.wrap(bigInt.toByteArray)
        case string: String     => ByteBuffer.wrap(string.getBytes)
        case bytes: Array[Byte] => ByteBuffer.wrap(bytes)
        case uuid: UUID =>
          val buffer: ByteBuffer = ByteBuffer.allocate(16)
          buffer.putLong(uuid.getMostSignificantBits)
          buffer.putLong(uuid.getMostSignificantBits)
          buffer
      }
    }
    buffer.rewind()
    buffer
  }

  private[this] def interpret(prevState: java.lang.Boolean): WriteOutcome = {
    if (prevState == null) {
      FirstTimeSeen
    } else if (prevState) {
      AlreadySeenWithChildren
    } else {
      AlreadySeenWithoutChildren
    }
  }
}

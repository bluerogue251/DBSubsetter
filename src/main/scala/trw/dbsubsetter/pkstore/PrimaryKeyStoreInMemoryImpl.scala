package trw.dbsubsetter.pkstore

import java.nio.ByteBuffer
import java.util.UUID

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

import scala.collection.mutable

private[pkstore] final class PrimaryKeyStoreInMemoryImpl(tables: Seq[Table]) extends PrimaryKeyStore {

  /*
   * If `seenWithChildrenStorage` contains a PK, then both its children AND its parents have been fetched.
   * If `seenWithoutChildrenStorage` contains a PK, then only its parents have been fetched
   *
   * There is no such thing as having fetched a row's children but not having fetched its parents. If a PK
   * is in there at all, then at any given time, it is either in `seenWithoutChildrenStorage` or in
   * `seenWithChildrenStorage` -- it will never be in both at once.
   */
  private[this] val seenWithoutChildrenStorage: Map[Table, mutable.HashSet[ByteBuffer]] =
    PrimaryKeyStoreInMemoryImpl.buildStorage(tables)

  private[this] val seenWithChildrenStorage: Map[Table, mutable.HashSet[ByteBuffer]] =
    PrimaryKeyStoreInMemoryImpl.buildStorage(tables)

  override def markSeen(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    this.synchronized {
      val rawValue: ByteBuffer = PrimaryKeyStoreInMemoryImpl.extract(primaryKeyValue)

      val alreadySeenWithChildren: Boolean =
        seenWithChildrenStorage(table).contains(rawValue)

      // Purposely lazy -- only do this extra work if logically necessary
      lazy val alreadySeenWithoutChildren =
        !seenWithoutChildrenStorage(table).add(rawValue)

      if (alreadySeenWithChildren) {
        AlreadySeenWithChildren
      } else if (alreadySeenWithoutChildren) {
        AlreadySeenWithoutChildren
      } else {
        FirstTimeSeen
      }
    }
  }

  override def markSeenWithChildren(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    this.synchronized {
      val rawValue: ByteBuffer = PrimaryKeyStoreInMemoryImpl.extract(primaryKeyValue)

      val alreadySeenWithChildren: Boolean =
        !seenWithChildrenStorage(table).add(rawValue)

      // Purposely lazy -- only do this extra work if logically necessary
      lazy val alreadySeenWithoutChildren: Boolean =
        seenWithoutChildrenStorage(table).remove(rawValue)

      if (alreadySeenWithChildren) {
        AlreadySeenWithChildren
      } else if (alreadySeenWithoutChildren) {
        AlreadySeenWithoutChildren
      } else {
        FirstTimeSeen
      }
    }
  }

  override def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean = {
    this.synchronized {
      val rawValue: ByteBuffer = PrimaryKeyStoreInMemoryImpl.extract(primaryKeyValue)
      seenWithChildrenStorage(table).contains(rawValue) || seenWithoutChildrenStorage(table).contains(rawValue)
    }
  }
}

private object PrimaryKeyStoreInMemoryImpl {
  private def buildStorage(tables: Seq[Table]): Map[Table, mutable.HashSet[ByteBuffer]] = {
    tables.map { t => t -> mutable.HashSet.empty[ByteBuffer] }.toMap
  }

  private def extract(primaryKeyValue: PrimaryKeyValue): ByteBuffer = {
    if (primaryKeyValue.individualColumnValues.size == 1) {
      extractSingle(primaryKeyValue.individualColumnValues.head)
    } else {
      val buffers: Seq[ByteBuffer] = primaryKeyValue.individualColumnValues.map(extractSingle)
      val count: Int = buffers.size
      val bufferSizes: Seq[Int] = buffers.map(_.position())
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
      compositeBuffer
    }
  }

  private def extractSingle(value: Any): ByteBuffer = {
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
}

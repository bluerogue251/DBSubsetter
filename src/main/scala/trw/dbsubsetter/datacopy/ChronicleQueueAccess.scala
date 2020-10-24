package trw.dbsubsetter.datacopy

import java.nio.file.Path

import net.openhft.chronicle.bytes.WriteBytesMarshallable
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue
import trw.dbsubsetter.bytes.PrimaryKeyBytes
import trw.dbsubsetter.chronicle.ChronicleQueueFactory
import trw.dbsubsetter.db.{PrimaryKey, PrimaryKeyValue}

private[datacopy] final class ChronicleQueueAccess(storageDirectory: Path, primaryKey: PrimaryKey) {

  private[this] val queue: SingleChronicleQueue =
    ChronicleQueueFactory.createQueue(storageDirectory)

  private[this] val appender = queue.acquireAppender()

  private[this] val tailer = queue.createTailer()

  def write(primaryKeyValues: Seq[PrimaryKeyValue]): Unit = {
    primaryKeyValues.foreach { primaryKeyValue =>
      val bytes: Array[Byte] = PrimaryKeyBytes.toBytes(primaryKeyValue)
      val woot: WriteBytesMarshallable = bytesOut => bytesOut.write(bytes)
      appender.writeBytes(woot)
    }
  }

  /**
    * @param n How many entries to return. Assumes prior knowledge that at least n entries exist in the queue to be
    *          read, and throws an exception if that is not the case.
    */
  def read(n: Short): Seq[PrimaryKeyValue] = {
    (1 to n).map { _ =>
      var optionalValue: Option[PrimaryKeyValue] = None

      tailer.readDocument { r =>
        val primaryKeyValue: PrimaryKeyValue = reader.apply(r.getValueIn)
        optionalValue = Some(primaryKeyValue)
      }

      if (optionalValue.isEmpty) {
        throw new RuntimeException("Read failed from chronicle data copy queue")
      }

      optionalValue.get
    }
  }
}

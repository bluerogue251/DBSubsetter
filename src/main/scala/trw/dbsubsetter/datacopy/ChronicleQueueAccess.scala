package trw.dbsubsetter.datacopy

import java.nio.file.Path

import net.openhft.chronicle.bytes.WriteBytesMarshallable
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.{SingleChronicleQueue, SingleChronicleQueueBuilder}
import trw.dbsubsetter.db.PrimaryKeyValue

private[datacopy] final class ChronicleQueueAccess(storageDirectory: Path, columnCount: Int) {

  private[this] val queue: SingleChronicleQueue =
    SingleChronicleQueueBuilder
      .binary(storageDirectory)
      .rollCycle(RollCycles.MINUTELY)
      .build()

  private[this] val appender = queue.acquireAppender()

  private[this] val tailer = queue.createTailer()

  def write(primaryKeyValues: Seq[PrimaryKeyValue]): Unit = {
    primaryKeyValues.foreach { primaryKeyValue =>
      primaryKeyValue.individualColumnValues.foreach { valueAsBytes =>
        val marshallable: WriteBytesMarshallable = bytesOut => bytesOut.write(valueAsBytes)
        appender.writeBytes(marshallable)
      }
    }
  }

  /**
    * @param n How many entries to return. Assumes prior knowledge that at least n entries exist in the queue to be
    *          read, and throws an exception if that is not the case.
    */
  def read(n: Short): Seq[PrimaryKeyValue] = {
    (1 to n).map(_ => readSingle())
  }

  private[this] def readSingle(): PrimaryKeyValue = {
    var primaryKeyValue: PrimaryKeyValue = null

    tailer.readDocument { r =>
      val individualColumnValues: Seq[Array[Byte]] = (0 until columnCount).map(_ => r.getValueIn.bytes())
      primaryKeyValue = new PrimaryKeyValue(individualColumnValues)
    }

    if (primaryKeyValue == null) {
      throw new RuntimeException("Read failed from chronicle data copy queue")
    }

    primaryKeyValue
  }
}

package trw.dbsubsetter.datacopy

import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue
import net.openhft.chronicle.wire.WireOut
import trw.dbsubsetter.chronicle.ChronicleQueueFactory
import trw.dbsubsetter.db.PrimaryKeyValue

import java.nio.file.Path

private[datacopy] final class ChronicleQueueAccess(dir: Path, reader: Function[Array[Byte], PrimaryKeyValue]) {

  private[this] val queue: SingleChronicleQueue =
    ChronicleQueueFactory.createQueue(dir)

  private[this] val appender = queue.acquireAppender()

  private[this] val tailer = queue.createTailer()

  def write(pkValues: Seq[PrimaryKeyValue]): Unit = {
    pkValues.foreach { pkValue =>
      appender.writeDocument { wire: WireOut =>
        wire.getValueOut.bytes(pkValue.x.bytes)
      }
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
        val pkValue: PrimaryKeyValue = reader(r.getValueIn.bytes())
        optionalValue = Some(pkValue)
      }

      if (optionalValue.isEmpty) {
        throw new RuntimeException("Read failed from chronicle data copy queue")
      }

      optionalValue.get
    }
  }
}

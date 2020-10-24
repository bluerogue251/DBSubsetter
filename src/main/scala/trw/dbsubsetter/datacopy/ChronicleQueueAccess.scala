package trw.dbsubsetter.datacopy

import java.nio.file.Path
import java.util.function.BiConsumer

import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue
import net.openhft.chronicle.wire.{ValueIn, ValueOut, WireOut}
import trw.dbsubsetter.chronicle.{ChronicleQueueFactory, ChronicleQueueFunctions}
import trw.dbsubsetter.db.{MultiColumnPrimaryKeyValue, PrimaryKey, PrimaryKeyValue}

private[datacopy] final class ChronicleQueueAccess(storageDirectory: Path, primaryKey: PrimaryKey) {

  private[this] val queue: SingleChronicleQueue =
    ChronicleQueueFactory.createQueue(storageDirectory)

  private[this] val writer: BiConsumer[ValueOut, PrimaryKeyValue] = {
    val singleColumnWriters: Seq[(ValueOut, MultiColumnPrimaryKeyValue) => WireOut] =
      columnTypes.map(ChronicleQueueFunctions.singleValueWrite)

    (valueOut, primaryKeyValue) => {
      singleColumnWriters
        .zip(primaryKeyValue.values)
        .foreach { case (singleColumnWriter, singleColumnValue) =>
          singleColumnWriter.apply(valueOut, singleColumnValue)
        }
    }
  }

  private[this] val reader: Function[ValueIn, PrimaryKeyValue] = {
    val singleColumnReaders: Seq[ValueIn => Any] =
      columnTypes.map(ChronicleQueueFunctions.singleValueRead)

    valueIn => {
      val individualColumnValues: Seq[Any] = singleColumnReaders.map(_.apply(valueIn))
      new MultiColumnPrimaryKeyValue(individualColumnValues)
    }
  }

  private[this] val appender = queue.acquireAppender()

  private[this] val tailer = queue.createTailer()

  def write(primaryKeyValues: Seq[MultiColumnPrimaryKeyValue]): Unit = {
    primaryKeyValues.foreach { primaryKeyValue =>
      appender.writeDocument(primaryKeyValue, writer)
    }
  }

  /**
    * @param n How many entries to return. Assumes prior knowledge that at least n entries exist in the queue to be
    *          read, and throws an exception if that is not the case.
    */
  def read(n: Short): Seq[MultiColumnPrimaryKeyValue] = {
    (1 to n).map { _ =>
      var optionalValue: Option[MultiColumnPrimaryKeyValue] = None

      tailer.readDocument { r =>
        val primaryKeyValue: MultiColumnPrimaryKeyValue = reader.apply(r.getValueIn)
        optionalValue = Some(primaryKeyValue)
      }

      if (optionalValue.isEmpty) {
        throw new RuntimeException("Read failed from chronicle data copy queue")
      }

      optionalValue.get
    }
  }
}

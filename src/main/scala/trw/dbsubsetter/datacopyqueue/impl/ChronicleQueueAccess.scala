package trw.dbsubsetter.datacopyqueue.impl

import java.util.function.BiConsumer

import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue
import net.openhft.chronicle.wire.ValueIn
import net.openhft.chronicle.wire.ValueOut
import net.openhft.chronicle.wire.WireOut
import trw.dbsubsetter.chronicle.ChronicleQueueFactory
import trw.dbsubsetter.chronicle.ChronicleQueueFunctions
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.ColumnTypes.ColumnType
import trw.dbsubsetter.db.PrimaryKeyValue

private[impl] final class ChronicleQueueAccess(config: Config, columnTypes: Seq[ColumnType]) {

  private[this] val queue: SingleChronicleQueue =
    ChronicleQueueFactory.createQueue(config)

  private[this] val writer: BiConsumer[ValueOut, PrimaryKeyValue] = {
    val singleColumnWriters: Seq[(ValueOut, Any) => WireOut] =
      columnTypes.map(ChronicleQueueFunctions.singleValueWrite)

    (valueOut, primaryKeyValue) => {
      singleColumnWriters
        .zip(primaryKeyValue.individualColumnValues)
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
      new PrimaryKeyValue(individualColumnValues)
    }
  }

  private[this] val appender = queue.acquireAppender()

  private[this] val tailer = queue.createTailer()

  def write(primaryKeyValues: Seq[PrimaryKeyValue]): Unit = {
    primaryKeyValues.foreach { primaryKeyValue =>
      appender.writeDocument(primaryKeyValue, writer)
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

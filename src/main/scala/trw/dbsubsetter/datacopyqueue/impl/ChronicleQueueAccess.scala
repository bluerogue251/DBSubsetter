package trw.dbsubsetter.datacopyqueue.impl

import java.nio.file.Files
import java.util.function.BiConsumer

import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.{SingleChronicleQueue, SingleChronicleQueueBuilder}
import net.openhft.chronicle.wire.{ValueIn, ValueOut, WireOut}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.ColumnTypes.ColumnType
import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.workflow.offheap.impl.chroniclequeue.ChronicleQueueFunctions


private[offheap] final class ChronicleQueueAccess(config: Config, columnTypes: Seq[ColumnType]) {

  private[this] val storageDir =
    config.taskQueueDirOpt match {
      case Some(dir) => dir.toPath
      case None => Files.createTempDirectory("DBSubsetter-")
    }

  private[this] val queue: SingleChronicleQueue =
    SingleChronicleQueueBuilder
      .binary(storageDir)
      .rollCycle(RollCycles.MINUTELY)
      .build()

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
      val individualColumnValues =
        singleColumnReaders.map(singleColumnReader => singleColumnReader.apply(valueIn))
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
    (0 to n).map { _ =>
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

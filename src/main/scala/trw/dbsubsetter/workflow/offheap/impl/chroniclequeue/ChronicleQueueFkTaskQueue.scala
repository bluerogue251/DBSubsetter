package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue
import net.openhft.chronicle.wire.WriteMarshallable
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue, SchemaInfo}
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueue
import trw.dbsubsetter.workflow.{ForeignKeyTask, RawTaskToForeignKeyTaskMapper}


private[offheap] final class ChronicleQueueFkTaskQueue(config: Config, schemaInfo: SchemaInfo) extends OffHeapFkTaskQueue {

  private[this] val queue: SingleChronicleQueue = ChronicleQueueFactory.createQueue(config)

  private[this] val appender = queue.acquireAppender()

  private[this] val tailer = queue.createTailer()

  private[this] val childReaders =
    schemaInfo
      .fksOrdered
      .map { fk =>
        new TaskQueueReader(fk.fromCols.map(_.dataType))
      }

  private[this] val parentReaders =
    schemaInfo.fksOrdered.map { fk =>
      new TaskQueueReader(fk.toCols.map(_.dataType))
    }

  private[this] val parentFkWriters =
    schemaInfo
      .fksOrdered
      .map { fk =>
        new TaskQueueWriter(fk.i, fk.toCols.map(_.dataType))
      }

  private[this] val childFkWriters =
    schemaInfo
      .fksOrdered
      .map { fk =>
        new TaskQueueWriter(fk.i, fk.fromCols.map(_.dataType))
      }

  override def enqueue(fkOrdinal: Short, fkValue: ForeignKeyValue, fetchChildren: Boolean): Unit = {
    val writer: TaskQueueWriter =
      if (fetchChildren) {
        childFkWriters(fkOrdinal)
      } else {
        parentFkWriters(fkOrdinal)
      }

    val writeMarshallable: WriteMarshallable =
      writer.writeHandler(fetchChildren, fkValue)

    appender.writeDocument(writeMarshallable)
  }

  override def dequeue(): Option[ForeignKeyTask] = {
    var optionalTask: Option[ForeignKeyTask] = None

    /*
     * `tailer.readDocument` can early return `false` if there is no new data on-disk to read. In this case,
     * `var optionalTask` stays set to `None` and is eventually returned as `None` from this method.
     */
    tailer.readDocument { r =>
      val in = r.getValueIn

      val fetchChildren: Boolean = in.bool()
      val fkOrdinal: Short = in.int16()

      val reader: TaskQueueReader =
        if (fetchChildren) {
          childReaders(fkOrdinal)
        } else {
          parentReaders(fkOrdinal)
        }

      val fkValue: ForeignKeyValue = reader.read(in)

      val foreignKey: ForeignKey = schemaInfo.fksOrdered(fkOrdinal)

      val task: ForeignKeyTask =
        RawTaskToForeignKeyTaskMapper.map(foreignKey, fetchChildren, fkValue)

      optionalTask = Some(task)
    }

    optionalTask
  }
}

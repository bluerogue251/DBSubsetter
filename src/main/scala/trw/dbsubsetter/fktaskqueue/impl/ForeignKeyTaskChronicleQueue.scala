package trw.dbsubsetter.fktaskqueue.impl

import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue
import net.openhft.chronicle.wire.WriteMarshallable
import trw.dbsubsetter.chronicle.ChronicleQueueFactory
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue, SchemaInfo}
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.workflow.{FetchChildrenTask, FetchParentTask, ForeignKeyTask}


/**
  * WARNING: this class is not threadsafe
  */
private[fktaskqueue] final class ForeignKeyTaskChronicleQueue(config: Config, schemaInfo: SchemaInfo) extends ForeignKeyTaskQueue {

  private[this] var queuedTaskCount: Long = 0L

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

  override def enqueue(foreignKeyTask: ForeignKeyTask): Unit = {
    foreignKeyTask match {
      case FetchChildrenTask(fk, fkValue) =>
        write(
          writer = childFkWriters(fk.i),
          fetchChildren = true,
          value = fkValue
        )
      case FetchParentTask(fk, fkValue) =>
        write(
          writer = parentFkWriters(fk.i),
          fetchChildren = false,
          value = fkValue
        )
    }

    queuedTaskCount += 1L
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
        if (fetchChildren) {
          FetchChildrenTask(foreignKey, fkValue)
        } else {
          FetchParentTask(foreignKey, fkValue)
        }

      queuedTaskCount -= 1L
      optionalTask = Some(task)
    }

    optionalTask
  }

  override def isEmpty(): Boolean = {
    queuedTaskCount == 0L
  }

  private[this] def write(writer: TaskQueueWriter, fetchChildren: Boolean, value: ForeignKeyValue): Unit = {
    val writeMarshallable: WriteMarshallable = writer.writeHandler(fetchChildren, value)
    appender.writeDocument(writeMarshallable)
  }
}

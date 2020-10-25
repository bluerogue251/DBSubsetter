package trw.dbsubsetter.fktaskqueue

import java.nio.file.Path

import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.{SingleChronicleQueue, SingleChronicleQueueBuilder}
import net.openhft.chronicle.wire.WriteMarshallable
import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue, SchemaInfo}
import trw.dbsubsetter.fkcalc.{FetchChildrenTask, FetchParentTask, ForeignKeyTask}

/**
  * WARNING: this class is not threadsafe
  */
private[fktaskqueue] final class ForeignKeyTaskQueueImpl(storageDirectory: Path, schemaInfo: SchemaInfo)
    extends ForeignKeyTaskQueue {

  private[this] var queuedTaskCount: Long = 0L

  private[this] val queue: SingleChronicleQueue =
    SingleChronicleQueueBuilder
      .binary(storageDirectory)
      .rollCycle(RollCycles.MINUTELY)
      .build()

  private[this] val appender = queue.acquireAppender()

  private[this] val tailer = queue.createTailer()

  override def enqueue(foreignKeyTask: ForeignKeyTask): Unit = {
    this.synchronized {
      foreignKeyTask match {
        case FetchParentTask(fk, fkValueFromChild) =>
          val marshallable: WriteMarshallable =
            wireOut => {
              val valueOut = wireOut.getValueOut
              valueOut.bool(false)
              valueOut.int16(fk.i)
              fkValueFromChild.individualColumnValues.foreach { valueAsBytes =>
                valueOut.bytes(valueAsBytes)
              }
            }
          appender.writeDocument(marshallable)

        case FetchChildrenTask(fk, fkValueFromParent) =>
          val marshallable: WriteMarshallable =
            wireOut => {
              val valueOut = wireOut.getValueOut
              valueOut.bool(true)
              valueOut.int16(fk.i)
              fkValueFromParent.individualColumnValues.foreach { valueAsBytes =>
                valueOut.bytes(valueAsBytes)
              }
            }
          appender.writeDocument(marshallable)
      }

      queuedTaskCount += 1L
    }
  }

  override def dequeue(): Option[ForeignKeyTask] = {
    this.synchronized {
      var optionalTask: Option[ForeignKeyTask] = None

      /*
       * `tailer.readDocument` can early return `false` if there is no new data on-disk to read. In this case,
       * `var optionalTask` stays set to `None` and is eventually returned as `None` from this method.
       */
      tailer.readDocument { r =>
        val in = r.getValueIn

        val fetchChildren: Boolean = in.bool()
        val fkOrdinal: Short = in.int16()
        val fk: ForeignKey = schemaInfo.fksOrdered(fkOrdinal)
        val columnCount = fk.fromCols.size
        val columnValues: Seq[Array[Byte]] = (1 to columnCount).map(_ => in.bytes())
        val fkValue: ForeignKeyValue = new ForeignKeyValue(columnValues)

        val task: ForeignKeyTask =
          if (fetchChildren) {
            FetchChildrenTask(fk, fkValue)
          } else {
            FetchParentTask(fk, fkValue)
          }

        queuedTaskCount -= 1L
        optionalTask = Some(task)
      }

      optionalTask
    }
  }

  override def nonEmpty(): Boolean = {
    this.synchronized {
      queuedTaskCount != 0L
    }
  }

  override def size(): Long = {
    this.synchronized {
      queuedTaskCount
    }
  }
}

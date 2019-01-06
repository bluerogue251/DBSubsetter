package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import java.nio.file.Files

import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import net.openhft.chronicle.wire.WriteMarshallable
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{ForeignKey, SchemaInfo}
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueue
import trw.dbsubsetter.workflow.{FkTask, NewTasks}


private[offheap] class FkTaskChronicleQueue(config: Config, schemaInfo: SchemaInfo) extends OffHeapFkTaskQueue {

  private[this] val storageDir = config.taskQueueDirOpt match {
    case Some(dir) => dir.toPath
    case None => Files.createTempDirectory("DBSubsetter-")
  }

  private[this] val queue =
    SingleChronicleQueueBuilder
      .binary(storageDir)
      .rollCycle(RollCycles.MINUTELY)
      .build()

  private[this] val appender =
    queue.acquireAppender()

  private[this] val tailer =
    queue.createTailer()

  private[this] val childReaders =
    schemaInfo.fksOrdered.map(fk => new TaskQueueReader(fk.fromCols.map(c => (c.jdbcType, c.typeName)), schemaInfo.dbVendor))

  private[this] val parentReaders =
    schemaInfo.fksOrdered.map(fk => new TaskQueueReader(fk.toCols.map(c => (c.jdbcType, c.typeName)), schemaInfo.dbVendor))

  private[this] val parentFkWriters =
    schemaInfo.fksOrdered.zipWithIndex.map { case (fk, i) => new TaskQueueWriter(i.toShort, fk.toCols.map(c => (c.jdbcType, c.typeName)), schemaInfo.dbVendor) }

  private[this] val childFkWriters =
    schemaInfo.fksOrdered.zipWithIndex.map { case (fk, i) => new TaskQueueWriter(i.toShort, fk.fromCols.map(c => (c.jdbcType, c.typeName)), schemaInfo.dbVendor) }

  override def enqueue(rawTasks: NewTasks): Unit = {
    val newTaskMap: Map[(ForeignKey, Boolean), Array[Any]] = rawTasks.taskInfo
    newTaskMap.foreach { case ((fk, fetchChildren), fkValues) =>
      val writer = if (fetchChildren) childFkWriters(fk.i) else parentFkWriters(fk.i)
      fkValues.foreach { fkValue =>
        val writeMarshallable: WriteMarshallable = writer.writeHandler(fetchChildren, fkValue)
        appender.writeDocument(writeMarshallable)
      }
    }
  }

  override def dequeue(): Option[FkTask] = {
    var task: FkTask = null

    /*
     * `tailer.readDocument` can early return `false` if there is no new data on-disk to read.
     * In this case, `var task` stays `null` and we therefore return a `Option#None` from this method.
     */
    tailer.readDocument { r =>
      val in = r.getValueIn
      val fetchChildren = in.bool()
      val fkOrdinal = in.int16()
      val reader = if (fetchChildren) childReaders(fkOrdinal) else parentReaders(fkOrdinal)
      val fkValue = reader.read(in)
      val fk = schemaInfo.fksOrdered(fkOrdinal)
      val table = if (fetchChildren) fk.fromTable else fk.toTable
      task = FkTask(table, fk, fkValue, fetchChildren)
    }

    Option(task)
  }
}

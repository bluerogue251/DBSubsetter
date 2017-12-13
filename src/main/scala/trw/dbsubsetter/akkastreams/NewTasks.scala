package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import net.openhft.chronicle.queue.ChronicleQueue
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object NewTasks {
  def flow(sch: SchemaInfo, numBaseQueries: Int, queue: ChronicleQueue): Flow[PkResult, Unit, NotUsed] = {
    Flow[PkResult].statefulMapConcat[Unit] { () =>
      // Queue writing utils
      val parentFkWriters = sch.fksOrdered.zipWithIndex.map { case (fk, i) => new TaskQueueWriter(i.toShort, fk.toCols.map(_.jdbcType)) }
      val childFkWriters = sch.fksOrdered.zipWithIndex.map { case (fk, i) => new TaskQueueWriter(i.toShort, fk.fromCols.map(_.jdbcType)) }
      val appender = queue.acquireAppender()

      // Stateful counter
      var unfinishedTaskCount: Long = numBaseQueries

      pkResult => {
        pkResult match {
          case pka: PksAdded =>
            val newTasks = NewFkTaskWorkflow.process(pka, sch)
            unfinishedTaskCount -= 1
            newTasks.foreach { case ((fk, fetchChildren), fkValues) =>
              unfinishedTaskCount += fkValues.size
              val writer = if (fetchChildren) childFkWriters(fk.i) else parentFkWriters(fk.i)
              fkValues.foreach { fkValue =>
                appender.writeDocument(writer.writeHandler(fetchChildren, fkValue))
              }
            }
          case DuplicateTask =>
            unfinishedTaskCount -= 1
          case other =>
            throw new RuntimeException(s"Cannot handle $other") // TODO: Make this a compile time error
        }

        if (unfinishedTaskCount == 0) {
          // Write to the queue that we are finished
          appender.writeDocument(w => w.getValueOut.bool(true))
        }

        Nil
      }
    }
  }
}

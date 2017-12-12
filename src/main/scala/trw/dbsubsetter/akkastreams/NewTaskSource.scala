package trw.dbsubsetter.akkastreams

import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import net.openhft.chronicle.queue.ChronicleQueue
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._


class NewTaskSource(sch: SchemaInfo, queue: ChronicleQueue) extends GraphStage[SourceShape[FkTask]] {
  // Akka streams details
  private val out = Outlet.create[FkTask]("ChronicleQueueTaskSource.out")
  override val shape: SourceShape[FkTask] = SourceShape.of(out)

  // Chronicle Queue Utils
  private val tailer = queue.createTailer()
  private val childReaders = sch.fksOrdered.map(fk => new TaskQueueReader(fk.fromCols.map(_.jdbcType).toArray))
  private val parentReaders = sch.fksOrdered.map(fk => new TaskQueueReader(fk.toCols.map(_.jdbcType).toArray))

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        tailer.readDocument { r =>
          val in = r.getValueIn
          val isComplete = in.bool()
          if (isComplete) {
            queue.close()
            completeStage()
          } else {
            val fetchChildren = in.bool()
            val fkOrdinal = in.int16()
            val reader = if (fetchChildren) childReaders(fkOrdinal) else parentReaders(fkOrdinal)
            val fkValue = reader.read(in)
            val fk = sch.fksOrdered(fkOrdinal)
            val table = if (fetchChildren) fk.fromTable else fk.toTable
            push[FkTask](out, FkTask(table, fk, fkValue, fetchChildren))
          }
        }
      }
    })
  }
}

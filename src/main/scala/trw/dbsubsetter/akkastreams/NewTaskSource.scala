package trw.dbsubsetter.akkastreams

import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler, TimerGraphStageLogic}
import akka.stream.{Attributes, Outlet, SourceShape}
import net.openhft.chronicle.queue.ChronicleQueue
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

import scala.concurrent.duration._

// Adapted from https://github.com/torodb/akka-chronicle-queue
class NewTaskSource(sch: SchemaInfo, queue: ChronicleQueue) extends GraphStage[SourceShape[FkTask]] {
  // Akka streams details
  private val out = Outlet.create[FkTask]("ChronicleQueueTaskSource.out")
  override val shape: SourceShape[FkTask] = SourceShape.of(out)

  // Chronicle Queue Utils
  private val tailer = queue.createTailer()
  private val childReaders = sch.fksOrdered.map(fk => new TaskQueueReader(fk.fromCols.map(_.jdbcType).toArray))
  private val parentReaders = sch.fksOrdered.map(fk => new TaskQueueReader(fk.toCols.map(_.jdbcType).toArray))

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new TimerGraphStageLogic(shape) {
    setHandler(out, new OutHandler {
      override def onPull(): Unit = doPull()
    })

    override protected def onTimer(timerKey: Any): Unit = doPull()

    private def doPull(): Unit = {
      val elementWasPresent = tailer.readDocument { r =>
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
      if (!elementWasPresent) scheduleOnce("pollChronicleQueue", Duration(10, MILLISECONDS))
    }
  }
}

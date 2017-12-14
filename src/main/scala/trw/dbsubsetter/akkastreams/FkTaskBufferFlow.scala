package trw.dbsubsetter.akkastreams

import java.nio.file.Files

import akka.stream._
import akka.stream.stage._
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import trw.dbsubsetter.db.{ForeignKey, SchemaInfo}
import trw.dbsubsetter.workflow._

// Adapted from https://github.com/torodb/akka-chronicle-queue
class FkTaskBufferFlow(sch: SchemaInfo) extends GraphStage[FlowShape[Map[(ForeignKey, Boolean), Vector[Any]], FkTask]] {
  private val in = Inlet.create[Map[(ForeignKey, Boolean), Vector[Any]]]("FkTaskBufferFlow.in")
  private val out = Outlet.create[FkTask]("FkTaskBufferFlow.out")
  override val shape: FlowShape[Map[(ForeignKey, Boolean), Vector[Any]], FkTask] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    // Chronicle Queue Utils
    private val storageDir = Files.createTempDirectory("DBSubsetter-")
    private val queue = SingleChronicleQueueBuilder.binary(storageDir).rollCycle(RollCycles.MINUTELY).build()
    private val appender = queue.acquireAppender()
    private val tailer = queue.createTailer()
    private val childReaders = sch.fksOrdered.map(fk => new TaskQueueReader(fk.fromCols.map(_.jdbcType).toArray))
    private val parentReaders = sch.fksOrdered.map(fk => new TaskQueueReader(fk.toCols.map(_.jdbcType).toArray))
    private val parentFkWriters = sch.fksOrdered.zipWithIndex.map { case (fk, i) => new TaskQueueWriter(i.toShort, fk.toCols.map(_.jdbcType)) }
    private val childFkWriters = sch.fksOrdered.zipWithIndex.map { case (fk, i) => new TaskQueueWriter(i.toShort, fk.fromCols.map(_.jdbcType)) }

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val newTaskMap = grab(in)
        newTaskMap.foreach { case ((fk, fetchChildren), fkValues) =>
          val writer = if (fetchChildren) childFkWriters(fk.i) else parentFkWriters(fk.i)
          fkValues.foreach { fkValue =>
            appender.writeDocument(writer.writeHandler(fetchChildren, fkValue))
          }
        }

        if (isAvailable(out)) doPull()
        pull(in)
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = doPull()
    })

    private def doPull(): Unit = {
      tailer.readDocument { r =>
        val in = r.getValueIn
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
}

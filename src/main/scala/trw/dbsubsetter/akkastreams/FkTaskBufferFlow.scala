package trw.dbsubsetter.akkastreams

import java.nio.file.Files

import akka.stream._
import akka.stream.stage._
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{ForeignKey, SchemaInfo}
import trw.dbsubsetter.workflow._

// Adapted from https://github.com/torodb/akka-chronicle-queue
class FkTaskBufferFlow(config: Config, sch: SchemaInfo) extends GraphStage[FlowShape[Map[(ForeignKey, Boolean), Array[Any]], ForeignKeyTask]] {
  val in: Inlet[Map[(ForeignKey, Boolean), Array[Any]]] = Inlet.create[Map[(ForeignKey, Boolean), Array[Any]]]("FkTaskBufferFlow.in")
  val out: Outlet[ForeignKeyTask] = Outlet.create[ForeignKeyTask]("FkTaskBufferFlow.out")
  override val shape: FlowShape[Map[(ForeignKey, Boolean), Array[Any]], ForeignKeyTask] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    private val storageDir = config.taskQueueDirOpt match {
      case None => Files.createTempDirectory("DBSubsetter-")
      case Some(dir) => dir.toPath
    }
    private val queue = SingleChronicleQueueBuilder.binary(storageDir).rollCycle(RollCycles.MINUTELY).build()
    private val appender = queue.acquireAppender()
    private val tailer = queue.createTailer()
    private val childReaders = sch.fksOrdered.map(fk => new TaskQueueReader(fk.fromCols.map(c => (c.jdbcType, c.typeName)), sch.dbVendor))
    private val parentReaders = sch.fksOrdered.map(fk => new TaskQueueReader(fk.toCols.map(c => (c.jdbcType, c.typeName)), sch.dbVendor))
    private val parentFkWriters = sch.fksOrdered.zipWithIndex.map { case (fk, i) => new TaskQueueWriter(i.toShort, fk.toCols.map(c => (c.jdbcType, c.typeName)), sch.dbVendor) }
    private val childFkWriters = sch.fksOrdered.zipWithIndex.map { case (fk, i) => new TaskQueueWriter(i.toShort, fk.fromCols.map(c => (c.jdbcType, c.typeName)), sch.dbVendor) }

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
      override def onPull(): Unit = {
        doPull()
      }
    })

    override def preStart(): Unit = {
      pull(in)
    }

    private def doPull(): Unit = {
      tailer.readDocument { r =>
        val in = r.getValueIn
        val fetchChildren = in.bool()
        val foreignKeyOrdinal = in.int16()
        val reader = if (fetchChildren) childReaders(foreignKeyOrdinal) else parentReaders(foreignKeyOrdinal)
        val fkValue = reader.read(in)
        val foreignKey = sch.fksOrdered(foreignKeyOrdinal)
        val task: ForeignKeyTask = if (fetchChildren)
          FetchChildrenTask(foreignKey, fkValue)
        else
          FetchParentTask(foreignKey, fkValue)
        push[ForeignKeyTask](out, task)
      }
    }
  }
}

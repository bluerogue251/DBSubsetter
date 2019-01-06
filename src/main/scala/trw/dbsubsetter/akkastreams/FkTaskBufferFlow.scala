package trw.dbsubsetter.akkastreams

import akka.stream._
import akka.stream.stage._
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueueFactory

// Adapted from https://github.com/torodb/akka-chronicle-queue
class FkTaskBufferFlow(config: Config, schemaInfo: SchemaInfo) extends GraphStage[FlowShape[NewTasks, ForeignKeyTask]] {

  private[this] val in: Inlet[NewTasks] = Inlet.create[NewTasks]("FkTaskBufferFlow.in")

  private[this] val out: Outlet[ForeignKeyTask] = Outlet.create[ForeignKeyTask]("FkTaskBufferFlow.out")

  override val shape: FlowShape[NewTasks, ForeignKeyTask] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    private[this] val offHeapFkTaskQueue =
      OffHeapFkTaskQueueFactory.buildOffHeapFkTaskQueue(config, schemaInfo)

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val newTasks: NewTasks = grab(in)
        offHeapFkTaskQueue.enqueue(newTasks)
        if (isAvailable(out)) doPull()
        pull(in)
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = doPull()
    })

    override def preStart(): Unit = {
      pull(in)
    }

    private[this] def doPull(): Unit = {
      val optionalTask: Option[ForeignKeyTask] = offHeapFkTaskQueue.dequeue()
      optionalTask.foreach(task => push[ForeignKeyTask](out, task))
    }
  }
}

package trw.dbsubsetter.akkastreams

import akka.{Done, NotUsed}
import akka.stream.FlowShape
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.stage.GraphStage
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.workflow.{DataCopyTask, ForeignKeyTask, PksAdded}

import scala.concurrent.Future

object BufferFactory {

  def dataCopyBufferSink(dataCopyQueue: DataCopyQueue): Sink[PksAdded, Future[Done]] = {
    Sink.foreach(dataCopyQueue.enqueue)
  }

  def dataCopyBufferSource(dataCopyQueue: DataCopyQueue): Source[DataCopyTask, NotUsed] = {
    val iterator: Iterator[DataCopyTask] =
      new Iterator[DataCopyTask] {
        override def hasNext: Boolean = !dataCopyQueue.isEmpty()

        override def next(): DataCopyTask = dataCopyQueue.dequeue().get
      }

    Source.fromIterator[DataCopyTask](() => iterator)
  }

  def fkTaskBuffer(fkTaskQueue: ForeignKeyTaskQueue): GraphStage[FlowShape[ForeignKeyTask, ForeignKeyTask]] = {
    val backingQueue: TransformingQueue[ForeignKeyTask, ForeignKeyTask] =
      TransformingQueue.from[ForeignKeyTask, ForeignKeyTask](
        fkTaskQueue.enqueue,
        fkTaskQueue.dequeue _,
        fkTaskQueue.isEmpty _
      )

    new QueueBackedBufferFlow[ForeignKeyTask, ForeignKeyTask](backingQueue)
  }
}

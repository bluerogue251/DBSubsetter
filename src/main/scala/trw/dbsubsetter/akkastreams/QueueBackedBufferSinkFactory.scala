package trw.dbsubsetter.akkastreams

import akka.Done
import akka.stream.scaladsl.Sink

import scala.concurrent.Future


private[akkastreams] object QueueBackedBufferSinkFactory {

  def build[T](backingQueue: TransformingQueue[T, _]): Sink[T, Future[Done]] = {
    Sink.foreach(backingQueue.enqueue)
  }
}
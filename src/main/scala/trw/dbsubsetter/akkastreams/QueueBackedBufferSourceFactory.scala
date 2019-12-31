package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Source


private[akkastreams] object QueueBackedBufferSourceFactory {

  def build[U](backingQueue: TransformingQueue[_, U]): Source[U, NotUsed] = {
    val iterator: Iterator[U] =
      new Iterator[U] {
        override def hasNext: Boolean = !backingQueue.isEmpty()

        override def next(): U = backingQueue.dequeue().get
      }

    Source.fromIterator[U](() => iterator)
  }
}
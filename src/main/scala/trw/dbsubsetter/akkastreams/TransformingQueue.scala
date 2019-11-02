package trw.dbsubsetter.akkastreams

/**
  * A queue which may transform its input elements into a different type before outputting them
  *
  * @tparam T The type of elements which are input to the queue
  * @tparam U The type of elements which are output from the queue
  */
trait TransformingQueue[T, U] {
  def enqueue(element: T): Unit
  def dequeue(): Option[U]
  def isEmpty(): Boolean
}

object TransformingQueue {
  def from[T, U](enqueueFunc: T => Unit, dequeueFunc: () => Option[U], isEmptyFunc: () => Boolean): TransformingQueue[T, U] = {
    new TransformingQueue[T, U] {
      override def enqueue(element: T): Unit = {
        enqueueFunc(element)
      }

      override def dequeue(): Option[U] = {
        dequeueFunc()
      }

      override def isEmpty(): Boolean = {
        isEmptyFunc()
      }
    }
  }
}
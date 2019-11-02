package trw.dbsubsetter.queue

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

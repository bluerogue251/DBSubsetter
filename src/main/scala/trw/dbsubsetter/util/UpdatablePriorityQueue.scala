package trw.dbsubsetter.util

/**
  * A priority queue which supports changing the priority of existing elements without removing them from the queue.
  * The priority is expressed via a Long. Higher values of the Long are prioritized in front of lower values.
  *
  * @tparam T The type of the element
  */
trait UpdatablePriorityQueue[T] {

  /**
    *
    * @return The highest priority element
    */
  def pop(): Option[T]

  /**
    * Increase the priority of an element in the
    * @param element the element of which to decrease the priority
    * @param n the amount by which to decrease the priority of the element
    *
    * The resulting priority level must remain non-negative. A {@link
    */
  def decreasePriority(element: T, n: Long)
}

package trw.dbsubsetter.util

object BatchingUtil {

  /**
    * @param elements An unbatched sequence of elements
    * @param batchSizes Desired batch sizes; must be provided in ascending order
    * @return The original elements in as few batches as possible, with each batch size
    *         guaranteed to be included in the supplied list of desired batch sizes.
    */
  def batch[T](elements: Seq[T], batchSizes: Seq[Short]): Seq[Seq[T]] = {
    if (elements.isEmpty || batchSizes.isEmpty || batchSizes.head != 1) {
      throw new IllegalArgumentException()
    }

    ???
  }
}

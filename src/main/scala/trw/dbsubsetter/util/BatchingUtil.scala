package trw.dbsubsetter.util

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

object BatchingUtil {

  /**
    * @param elements   An unbatched sequence of elements
    * @param batchSizes Desired batch sizes; must be provided in descending order
    * @return The original elements in as few batches as possible, with each batch size
    *         guaranteed to be included in the supplied list of desired batch sizes.
    */
  def batch[T](elements: Seq[T], batchSizes: Seq[Short]): Seq[Seq[T]] = {
    if (elements.isEmpty || batchSizes.isEmpty) {
      throw new IllegalArgumentException()
    }

    if (batchSizes.contains(elements.size)) {
      Seq(elements)
    } else {
      batchInternal(elements, batchSizes, ArrayBuffer[Seq[T]]())
    }
  }

  @tailrec
  private[this] def batchInternal[T](elems: Seq[T], batchSizes: Seq[Short], accum: ArrayBuffer[Seq[T]]): Seq[Seq[T]] = {
    if (elems.isEmpty) {
      accum
    } else {
      val currentBatchSize: Short = batchSizes.head
      val (currentBatch, remainingElems): (Seq[T], Seq[T]) = elems.splitAt(currentBatchSize)
      if (currentBatch.size < currentBatchSize) {
        batchInternal(elems, batchSizes.tail, accum)
      } else {
        accum += currentBatch
        batchInternal(remainingElems, batchSizes, accum)
      }
    }
  }
}

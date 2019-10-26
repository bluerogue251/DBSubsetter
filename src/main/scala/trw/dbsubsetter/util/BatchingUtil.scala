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

    batchInternal(elements, batchSizes, ArrayBuffer[Seq[T]]())
  }

  @tailrec
  private[this] def batchInternal[T](elems: Seq[T], batchSizes: Seq[Short], accum: ArrayBuffer[Seq[T]]): Seq[Seq[T]] = {
    if (elems.isEmpty) {
      accum
    } else if (batchSizes.isEmpty) {
      throw new RuntimeException("Failed to batch elements")
    } else {
      val currentBatchSize: Short = batchSizes.head
      val newBatches: IndexedSeq[Seq[T]] = elems.grouped(currentBatchSize).toIndexedSeq
      for (i <- 0 until newBatches.length - 1) {
        val definitelyWholeBatch: Seq[T] = newBatches(i)
        accum += definitelyWholeBatch
      }
      val batchOfUnknownSize = newBatches.last
      if (batchOfUnknownSize.size == currentBatchSize) {
        accum += batchOfUnknownSize
        batchInternal(Seq.empty, Seq.empty, accum)
      } else {
        batchInternal(batchOfUnknownSize, batchSizes.tail, accum)
      }
    }
  }
}

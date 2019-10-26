package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.util.BatchingUtil

class BatchingUtilTest extends FunSuite {
  private[this] val OneElement = Seq[Int](1)

  private[this] val ValidBatchSizes = Seq[Short](1, 2, 4)

  test("Batching Util Does Not Accept Empty Input Elements") {
    assertThrows[IllegalArgumentException](BatchingUtil.batch(Seq.empty[Int], ValidBatchSizes))
  }

  test("Batching Util Does Not Accept Empty Batch Sizes") {
    assertThrows[IllegalArgumentException](BatchingUtil.batch(OneElement, Seq.empty[Short]))
  }

  test("Batching Util Ensures that First Batch Size is 1") {
    assertThrows[IllegalArgumentException](BatchingUtil.batch(OneElement, Seq[Short](2)))
  }
}

package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.util.BatchingUtil

class BatchingUtilTest extends FunSuite {
  private[this] val ValidBatchSizes = Seq[Short](4, 2, 1)

  private[this] val ValidElements = Seq[Int](1, 2, 3, 4, 5, 6)

  test("Batching Util Does Not Accept Empty Input Elements") {
    assertThrows[IllegalArgumentException](BatchingUtil.batch(Seq.empty[Int], ValidBatchSizes))
  }

  test("Batching Util Does Not Accept Empty Batch Sizes") {
    assertThrows[IllegalArgumentException](BatchingUtil.batch(ValidElements, Seq.empty[Short]))
  }

  test("Batching Util works with one element") {
    val elements: Seq[Int] = Seq(1)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1)))
  }

  test("Batching Util works with two elements") {
    val elements: Seq[Int] = Seq(1, 2)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1, 2)))
  }

  test("Batching Util works with three elements") {
    val elements: Seq[Int] = Seq(1, 2, 3)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1, 2), Seq(3)))
  }

  test("Batching Util works with four elements") {
    val elements: Seq[Int] = Seq(1, 2, 3, 4)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1, 2, 3, 4)))
  }

  test("Batching Util works with five elements") {
    val elements: Seq[Int] = Seq(1, 2, 3, 4, 5)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1, 2, 3, 4), Seq(5)))
  }

  test("Batching Util works with six elements") {
    val elements: Seq[Int] = Seq(1, 2, 3, 4, 5, 6)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1, 2, 3, 4), Seq(5, 6)))
  }

  test("Batching Util works with seven elements") {
    val elements: Seq[Int] = Seq(1, 2, 3, 4, 5, 6, 7)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1, 2, 3, 4), Seq(5, 6), Seq(7)))
  }

  test("Batching Util works with eight elements") {
    val elements: Seq[Int] = Seq(1, 2, 3, 4, 5, 6, 7, 8)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1, 2, 3, 4), Seq(5, 6, 7, 8)))
  }

  test("Batching Util works with nine elements") {
    val elements: Seq[Int] = Seq(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1, 2, 3, 4), Seq(5, 6, 7, 8), Seq(9)))
  }

  test("Batching Util works with ten elements") {
    val elements: Seq[Int] = Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val result: Seq[Seq[Int]] = BatchingUtil.batch(elements, ValidBatchSizes)
    assert(result === Seq(Seq(1, 2, 3, 4), Seq(5, 6, 7, 8), Seq(9, 10)))
  }
}

package trw.dbsubsetter.pkstore

import org.scalatest.FunSuite
import trw.dbsubsetter.db.PrimaryKeyValue

class PkStoreTest extends FunSuite {
  test("PkStore is conscious of whether children have been processed yet") {
    val pkStore: PkStore = PkStore.empty()
    val pkValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[String]("pkValue"))

    // Add the PK to the pkStore, noting that we are not planning to fetch its children at this time
    val writeOutcome1 = pkStore.markSeenWithoutChildren(pkValue)
    assert(writeOutcome1 === FirstTimeSeen)

    // Add the PK to the pkStore again, noting that we are still not planning to fetch its children at this time
    val writeOutcome2 = pkStore.markSeenWithoutChildren(pkValue)
    assert(writeOutcome2 === AlreadySeenWithoutChildren)

    // Add the PK to the pkStore, noting that we are now planning to fetch its children
    val writeOutcome3 = pkStore.markSeenWithChildren(pkValue)
    assert(writeOutcome3 === AlreadySeenWithoutChildren)

    // Add the PK to the pkStore, noting that we are again planning to fetch its children
    val writeOutcome4 = pkStore.markSeenWithChildren(pkValue)
    assert(writeOutcome4 === AlreadySeenWithChildren)
  }

  test("PkStore accurately reports what it has seen previously") {
    val pkStore: PkStore = PkStore.empty()

    val firstStringValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[String]("first-value"))
    val otherStringValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[String]("other-value"))
    assert(pkStore.alreadySeen(firstStringValue) === false)
    assert(pkStore.alreadySeen(otherStringValue) === false)
    pkStore.markSeenWithoutChildren(firstStringValue)
    assert(pkStore.alreadySeen(firstStringValue) === true)
    assert(pkStore.alreadySeen(otherStringValue) === false)

    val firstIntValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[Int](1))
    val otherIntValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[Int](2))
    assert(pkStore.alreadySeen(firstIntValue) === false)
    assert(pkStore.alreadySeen(otherIntValue) === false)
    pkStore.markSeenWithoutChildren(firstIntValue)
    assert(pkStore.alreadySeen(firstIntValue) === true)
    assert(pkStore.alreadySeen(otherIntValue) === false)

    val firstMultiIntValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[Int](1, 2))
    val otherMultiIntValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[Int](2, 3))
    assert(pkStore.alreadySeen(firstMultiIntValue) === false)
    assert(pkStore.alreadySeen(otherMultiIntValue) === false)
    pkStore.markSeenWithoutChildren(firstMultiIntValue)
    assert(pkStore.alreadySeen(firstMultiIntValue) === true)
    assert(pkStore.alreadySeen(otherMultiIntValue) === false)
  }

  test("PkStore can handle millions of entries") {
    val pkStore: PkStore = PkStore.empty()

    (0 until 2500000).foreach { i =>
      val value: PrimaryKeyValue = new PrimaryKeyValue(Seq(i))
      assert(pkStore.alreadySeen(value) === false)
      assert(pkStore.markSeenWithChildren(value) === FirstTimeSeen)
      assert(pkStore.alreadySeen(value) === true)
      assert(pkStore.markSeenWithChildren(value) === AlreadySeenWithChildren)
      assert(pkStore.markSeenWithoutChildren(value) === AlreadySeenWithChildren)
    }

    (2500000 until 5000000).foreach { i =>
      val value: PrimaryKeyValue = new PrimaryKeyValue(Seq(i))
      assert(pkStore.alreadySeen(value) === false)
      assert(pkStore.markSeenWithoutChildren(value) === FirstTimeSeen)
      assert(pkStore.alreadySeen(value) === true)
      assert(pkStore.markSeenWithoutChildren(value) === AlreadySeenWithoutChildren)
      assert(pkStore.markSeenWithoutChildren(value) === AlreadySeenWithoutChildren)
      assert(pkStore.markSeenWithChildren(value) === AlreadySeenWithoutChildren)
      assert(pkStore.markSeenWithChildren(value) === AlreadySeenWithChildren)
    }
  }
}

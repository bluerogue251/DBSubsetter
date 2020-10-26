package trw.dbsubsetter.pkstore

import org.scalatest.FunSuite
import trw.dbsubsetter.db.{PrimaryKeyValue, Schema, Table}

class PkStoreStressTest extends FunSuite {
  test("PkStore is conscious of whether children have been processed yet") {
    val table: Table =
      Table(
        schema = Schema("public"),
        name = "users"
      )

    val pkStore: MultiTablePkStore =
      MultiTablePkStore.from(Seq(table))

    val pkValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[String]("pkValue"))

    // Add the PK to the pkStore, noting that we are not planning to fetch its children at this time
    val writeOutcome1 = pkStore.markSeen(table, pkValue)
    assert(writeOutcome1 === FirstTimeSeen)

    // Add the PK to the pkStore again, noting that we are still not planning to fetch its children at this time
    val writeOutcome2 = pkStore.markSeen(table, pkValue)
    assert(writeOutcome2 === AlreadySeenWithoutChildren)

    // Add the PK to the pkStore, noting that we are now planning to fetch its children
    val writeOutcome3 = pkStore.markSeenWithChildren(table, pkValue)
    assert(writeOutcome3 === AlreadySeenWithoutChildren)

    // Add the PK to the pkStore, noting that we are again planning to fetch its children
    val writeOutcome4 = pkStore.markSeenWithChildren(table, pkValue)
    assert(writeOutcome4 === AlreadySeenWithChildren)
  }

  test("PkStore accurately reports what it has seen previously") {
    val table: Table = Table(schema = Schema("public"), name = "users")
    val pkStore: MultiTablePkStore = MultiTablePkStore.from(Seq(table))

    val firstStringValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[String]("first-value"))
    val otherStringValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[String]("other-value"))
    assert(pkStore.alreadySeen(table, firstStringValue) === false)
    assert(pkStore.alreadySeen(table, otherStringValue) === false)
    pkStore.markSeen(table, firstStringValue)
    assert(pkStore.alreadySeen(table, firstStringValue) === true)
    assert(pkStore.alreadySeen(table, otherStringValue) === false)

    val firstIntValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[Int](1))
    val otherIntValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[Int](2))
    assert(pkStore.alreadySeen(table, firstIntValue) === false)
    assert(pkStore.alreadySeen(table, otherIntValue) === false)
    pkStore.markSeen(table, firstIntValue)
    assert(pkStore.alreadySeen(table, firstIntValue) === true)
    assert(pkStore.alreadySeen(table, otherIntValue) === false)

    val firstMultiIntValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[Int](1, 2))
    val otherMultiIntValue: PrimaryKeyValue = new PrimaryKeyValue(Seq[Int](2, 3))
    assert(pkStore.alreadySeen(table, firstMultiIntValue) === false)
    assert(pkStore.alreadySeen(table, otherMultiIntValue) === false)
    pkStore.markSeen(table, firstMultiIntValue)
    assert(pkStore.alreadySeen(table, firstMultiIntValue) === true)
    assert(pkStore.alreadySeen(table, otherMultiIntValue) === false)
  }
}

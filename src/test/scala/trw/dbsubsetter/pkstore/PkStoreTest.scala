package trw.dbsubsetter.pkstore

import org.scalatest.FunSuite
import trw.dbsubsetter.db.{PrimaryKeyValue, Schema, Table}

class PkStoreTest extends FunSuite {
  test("PkStore is conscious of whether children have been processed yet") {
    val table: Table =
      Table(
        schema = Schema("public"),
        name = "users"
      )

    val pkStore: PrimaryKeyStore =
      PrimaryKeyStore.from(Seq(table))

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
}

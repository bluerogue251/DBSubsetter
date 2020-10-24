package trw.dbsubsetter.pkstore

import org.scalatest.FunSuite
import trw.dbsubsetter.db.{PrimaryKeyValue, Schema, Table}

class PkStoreGarbageCollectionTest extends FunSuite {
  test("Test GC Overhead") {
    val table: Table =
      Table(
        schema = Schema("public"),
        name = "users"
      )

    val pkStore: PrimaryKeyStore =
      PrimaryKeyStore.from(Seq(table))

    var i: Int = 0
    var j: Int = 0

    while (i < 100000000) {
      val pkValue: PrimaryKeyValue = new PrimaryKeyValue(Seq(s"$i-woot"))
      if (j == 0) {
        pkStore.markSeen(table, pkValue)
      } else if (j == 1) {
        pkStore.markSeenWithChildren(table, pkValue)
      } else {
        pkStore.alreadySeen(table, pkValue)
      }
      i += 1
      if (j < 2) {
        j += 1
      } else {
        j = 0
      }
    }
  }
}

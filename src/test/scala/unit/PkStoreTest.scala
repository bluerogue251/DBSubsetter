package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.db.{Column, SchemaInfo, Table}
import trw.dbsubsetter.primarykeystore._

class PkStoreTest extends FunSuite {
  test("PkStore is conscious of whether children have been processed yet") {
    val table = Table("public", "users", hasSqlServerAutoIncrement = true, storePks = true)
    val pkCol = Column(table, null, 0, null, null)
    val schemaInfo = SchemaInfo(Map.empty, Map.empty, Map(table -> Vector(pkCol)), Array.empty, Map.empty, Map.empty, null)
    val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.buildPrimaryKeyStore(schemaInfo)

    val pkValue = "pkValue"

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

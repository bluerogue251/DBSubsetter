package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.db.{Column, SchemaInfo, Table}
import trw.dbsubsetter.primarykeystore._

class PkStoreTest extends FunSuite {
  test("PkStore#markSeen is conscious of children part 1") {
    val table = Table("public", "users", hasSqlServerAutoIncrement = true, storePks = true)
    val pkCol = Column(table, null, 0, null, null)
    val schemaInfo = SchemaInfo(Map.empty, Map.empty, Map(table -> Vector(pkCol)), Array.empty, Map.empty, Map.empty, null)
    val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.buildPrimaryKeyStore(schemaInfo)

    val pkValue = "pkValue"

    // Add the PK to the pkStore, noting that we are not planning on fetching children
    val writeOutcome1 = pkStore.markSeen(table, pkValue)
    assert(writeOutcome1 === FirstTimeSeen)

    // Add the PK to the pkStore again, noting that we are still not planning on fetching children
    val writeOutcome2 = pkStore.markSeen(table, pkValue)
    assert(writeOutcome2 === AlreadySeenWithoutChildren)

    // Add the PK to the pkStore, noting that we are now planning on fetching children
    val writeOutcome3 = pkStore.markSeenWithChildren(table, pkValue)
    assert(writeOutcome3 === AlreadySeenWithoutChildren)

    // Add the PK to the pkStore, noting that we are also now planning on fetching children
    val writeOutcome4 = pkStore.markSeenWithChildren(table, pkValue)
    assert(writeOutcome4 === AlreadySeenWithChildren)
  }
}

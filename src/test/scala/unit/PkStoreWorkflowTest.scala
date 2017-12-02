package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.db.{Row, Table}
import trw.dbsubsetter.workflow._

class PkStoreWorkflowTest extends FunSuite {
  test("PkStore is conscious of fetchChildren for `exists` requests") {
    val table = Table("public", "users", hasSqlServerAutoIncrement = true)
    val map = Map(table -> Seq(0))
    val pkStore = new PkStoreWorkflow(map)
    val fkValue = "fkValue"
    val row: Row = Array(fkValue)
    val rows = Vector(row)

    // Add the PK to the pkStore, noting that we have NOT yet fetched children
    val pkAddRequest1 = OriginDbResult(table, rows, fetchChildren = false)
    val pkAddResult1 = pkStore.add(pkAddRequest1)
    assert(pkAddResult1 === PksAdded(table, rows, Vector.empty))

    // Query whether the PK is in the pkStore given that we are only interested in parent records
    // The empty list result tells us that the PK exists already
    val pkQueryRequest1 = FkTask(table, null, fkValue, fetchChildren = false)
    val pkQueryResult1 = pkStore.exists(pkQueryRequest1)
    assert(pkQueryResult1 === DuplicateTask)

    // This time query whether the PK is in the pkStore given that we ARE interested in both parents AND children
    // The result tells us that the PK's children have not yet been fetched.
    // Even though its parents have already been fetched, that is not relevant
    val pkQueryRequest2 = FkTask(table, null, fkValue, fetchChildren = true)
    val pkQueryResult2 = pkStore.exists(pkQueryRequest2)
    assert(pkQueryResult2 === pkQueryRequest2)

    // Now we add the the PK to the pkStore noting that we will fetch children for it
    // The fact that it was already in the PK store for having its parents fetched means that
    // It will only appear in the collection of rows still needing children processing
    // It will not appear in the collection of rows needing parents (and therefore will not be added duplicate to the target db either)
    val pkAddRequest2 = OriginDbResult(table, rows, fetchChildren = true)
    val pkAddResult2 = pkStore.add(pkAddRequest2)
    assert(pkAddResult2 === PksAdded(table, Vector.empty, rows))

    // Now we query again and since we've already fetched children, we should never get anything back from the queries
    val pkQueryResult3 = pkStore.exists(pkQueryRequest1)
    assert(pkQueryResult3 === DuplicateTask)
    val pkQueryResult4 = pkStore.exists(pkQueryRequest2)
    assert(pkQueryResult4 === DuplicateTask)
  }
}

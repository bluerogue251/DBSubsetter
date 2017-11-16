package unit.workflow

import org.scalatest.FunSuite
import trw.dbsubsetter.db.{Row, Table}
import trw.dbsubsetter.workflow.{FkTask, OriginDbResult, PkStoreWorkflow, PksAdded}

class PkStoreWorkflowTest extends FunSuite {
  test("PkStore is conscious of fetchChildren for `exists` requests") {
    val table = Table("public", "users")
    val map = Map(table -> Seq(0))
    val pkStore = new PkStoreWorkflow(map)
    val fkValue = "fkValue"
    val row: Row = Array(fkValue)
    val rows = Vector(row)

    // Add the PK to the pkStore, noting that we have NOT yet fetched children
    val pkAddRequest1 = OriginDbResult(table, rows, fetchChildren = false)
    val pkAddResult1 = pkStore.process(pkAddRequest1)
    assert(pkAddResult1 === List(PksAdded(table, rows, fetchChildren = false)))

    // Query whether the PK is in the pkStore given that we are only interested in parent records
    // The empty list result tells us that the PK exists already
    val pkQueryRequest1 = FkTask(table, null, fkValue, fetchChildren = false)
    val pkQueryResult1 = pkStore.process(pkQueryRequest1)
    assert(pkQueryResult1 === List.empty)

    // This time query whether the PK is in the pkStore given that we ARE interested in both parents AND children
    // The result tells us that the PK's children have not yet been fetched.
    // Even though its parents have already been fetched, that is not relevant
    val pkQueryRequest2 = FkTask(table, null, fkValue, fetchChildren = true)
    val pkQueryResult2 = pkStore.process(pkQueryRequest2)
    assert(pkQueryResult2 === List(pkQueryRequest2))

    // Now we add the the PK to the pkStore noting that we have fetched children already
    val pkAddRequest2 = OriginDbResult(table, rows, fetchChildren = true)
    val pkAddResult2 = pkStore.process(pkAddRequest2)
    assert(pkAddResult2 === List(PksAdded(table, rows, fetchChildren = true)))

    // Now we query again and since we've already fetched children, we should never get anything back from the queries
    val pkQueryResult3 = pkStore.process(pkQueryRequest1)
    assert(pkQueryResult3 === List.empty)
    val pkQueryResult4 = pkStore.process(pkQueryRequest2)
    assert(pkQueryResult4 === List.empty)
  }
}

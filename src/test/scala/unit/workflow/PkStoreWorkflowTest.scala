package unit.workflow

import org.scalatest.FunSuite
import trw.dbsubsetter.db.{Row, Table}
import trw.dbsubsetter.workflow
import trw.dbsubsetter.workflow.{FkTask, OriginDbResult, PkStoreWorkflow}

class PkStoreWorkflowTest extends FunSuite {
  test("PkStore is conscious of fetchChildren for `exists` requests") {
    val table = Table("public", "users")
    val map = Map(table -> Seq(0))
    val pkStore = new PkStoreWorkflow(map)
    val fkValue = "fkValue"
    val row: Row = Array(fkValue)

    // Add the PK to the pkStore, noting that we have NOT yet fetched children
    val pkAddRequest1 = OriginDbResult(table, Vector(row), fetchChildren = false)
    val pkAddResult1 = pkStore.process(pkAddRequest1)
    assert(pkAddResult1 === List(PksAdded(table, Vector(row), fetchChildren = false)))

    // Query whether the PK is in the pkStore given that we are NOT interested in children records
    // The empty list result tells us that the PK exists already
    val pkQueryRequest1 = FkTask(table, null, fkValue, fetchChildren = false)
    val pkQueryResult1 = pkStore.process(pkQueryRequest1)
    assert(pkQueryResult1 === List.empty)

    // This time query whether the PK is in the pkStore given that we ARE interested in children records
    // The non-empty list result tells us that the PK either does not exist at all,
    // or it exists but has not yet had children fetched
    val pkQueryRequest2 = workflow.FkTask(table, null, fkValue, fetchChildren = true)
    val pkQueryResult2 = pkStore.process(pkQueryRequest1)
    assert(pkQueryResult1 === List(pkQueryRequest2))

    // Now we add the the PK to the pkStore, noting that we HAVE fetched children already
    val pkAddRequest2 = OriginDbResult(table, Vector(row), fetchChildren = true)
    val pkAddResult2 = pkStore.process(pkAddRequest2)
    assert(pkAddResult2 === List(PksAdded(table, Vector(row), fetchChildren = true)))

    // Now we query again
    val pkQueryResult3 = pkStore.process(pkQueryRequest1)
    assert(pkQueryResult3 ===)
    val pkQueryResult4 = pkStore.process(pkQueryRequest2)
    assert(pkQueryResult3 ===)
  }
}

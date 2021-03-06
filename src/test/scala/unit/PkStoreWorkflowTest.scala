package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.OriginDbResult
import trw.dbsubsetter.db.{Column, Keys, PrimaryKey, PrimaryKeyValue, Schema, SchemaInfo, Table}
import trw.dbsubsetter.pkstore.{PkStoreWorkflow, PksAdded}

class PkStoreWorkflowTest extends FunSuite {
  test("PkStoreWorkflow is conscious of fetchChildren") {
    val table: Table =
      Table(
        schema = Schema("public"),
        name = "users"
      )

    val pkCol: Column =
      new Column(
        table = table,
        name = null,
        dataType = null
      )

    val schemaInfo: SchemaInfo =
      new SchemaInfo(
        tables = Seq.empty,
        keyColumnsByTable = Map.empty,
        dataColumnsByTable = Map.empty,
        pksByTable = Map(table -> new PrimaryKey(Seq(pkCol))),
        foreignKeys = Seq.empty,
        fksFromTable = Map.empty,
        fksToTable = Map.empty
      )

    val pkStoreWorkflow =
      PkStoreWorkflow.from(schemaInfo)

    val fkValue: String = "fkValue"

    val singleRowKeys: Keys = new Keys(Map(pkCol -> fkValue))

    val multiRowKeys: Vector[Keys] = Vector(singleRowKeys)

    val correspondingPrimaryKeyValue: PrimaryKeyValue = new PrimaryKeyValue(Seq(fkValue))

    // Add the PK to the pkStore, noting that we have NOT yet fetched children
    val pkAddRequest1 = OriginDbResult(table, multiRowKeys, None, fetchChildren = false)
    val pkAddResult1 = pkStoreWorkflow.add(pkAddRequest1)
    assert(pkAddResult1 === PksAdded(table, multiRowKeys, Vector.empty, None))

    // Query whether the PK is in the pkStore given that we are only interested in parent records
    // The return value should be true, meaning that yes it's in the pkStore at least as far as having fetched its parent records
    assert(pkStoreWorkflow.alreadySeen(table, correspondingPrimaryKeyValue) === true)

    // Now we add the the PK to the pkStore noting that we will fetch children for it
    // The fact that it was already in the PK store for having its parents fetched means that
    // It will only appear in the collection of rows still needing children processing
    // It will not appear in the collection of rows needing parents (and therefore will not be added duplicate to the target db either)
    val pkAddRequest2 = OriginDbResult(table, multiRowKeys, None, fetchChildren = true)
    val pkAddResult2 = pkStoreWorkflow.add(pkAddRequest2)
    assert(pkAddResult2 === PksAdded(table, Vector.empty, multiRowKeys, None))

    // Do the same query as before
    // Query whether the PK is in the pkStore given that we are only interested in parent records
    // The return value should be true, meaning that yes it's in the pkStore at least as far as having fetched its parent records
    // (This query only returns info about having fetched parent records, but it should remain true even though the last thing we did was fetch children)
    assert(pkStoreWorkflow.alreadySeen(table, correspondingPrimaryKeyValue) === true)
  }

  test("PkStoreWorkflow is conscious of fetchChildren part2") {
    val table: Table =
      Table(
        schema = Schema("public"),
        name = "users"
      )

    val primaryKeyColumn: Column =
      new Column(
        table = table,
        name = null,
        dataType = null
      )

    val schemaInfo: SchemaInfo =
      new SchemaInfo(
        tables = Seq.empty,
        keyColumnsByTable = Map.empty,
        dataColumnsByTable = Map.empty,
        pksByTable = Map(table -> new PrimaryKey(Seq(primaryKeyColumn))),
        foreignKeys = Seq.empty,
        fksFromTable = Map.empty,
        fksToTable = Map.empty
      )

    val pkStoreWorkflow: PkStoreWorkflow =
      PkStoreWorkflow.from(schemaInfo)

    val fkValue: String = "fkValue"
    val singleRowKeys: Keys = new Keys(Map(primaryKeyColumn -> fkValue))
    val multiRowKeys: Vector[Keys] = Vector(singleRowKeys)

    // Add the PK to the pkStore, noting that we have NOT yet fetched children
    val pkAddRequest1 = OriginDbResult(table, multiRowKeys, viaTableOpt = None, fetchChildren = true)
    val actual = pkStoreWorkflow.add(pkAddRequest1)
    val expected: PksAdded = PksAdded(
      table = table,
      rowsNeedingParentTasks = multiRowKeys,
      rowsNeedingChildTasks = multiRowKeys,
      viaTableOpt = None
    )
    assert(actual === expected)
  }
}

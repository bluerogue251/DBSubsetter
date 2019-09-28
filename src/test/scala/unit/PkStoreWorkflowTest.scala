package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{Column, Row, SchemaInfo, Table}
import trw.dbsubsetter.primarykeystore.{PrimaryKeyStore, PrimaryKeyStoreFactory}
import trw.dbsubsetter.workflow._

class PkStoreWorkflowTest extends FunSuite {
  test("PkStoreWorkflow is conscious of fetchChildren") {
    val table: Table =
      new Table(
        schema = "public",
        name = "users",
        hasSqlServerAutoIncrement = true,
        storePks = true
      )

    val pkCol: Column =
      new Column(
        table = table,
        name = null,
        ordinalPosition = 0,
        jdbcType = null,
        typeName = null
      )

    val schemaInfo: SchemaInfo =
      new SchemaInfo(
        tablesByName = Map.empty,
        colsByTableOrdered = Map.empty,
        pksByTableOrdered = Map(table -> Vector(pkCol)),
        fksOrdered = Array.empty,
        fksFromTable = Map.empty,
        fksToTable = Map.empty,
        dbVendor = null
      )

    val pkStore: PrimaryKeyStore =
      PrimaryKeyStoreFactory.buildPrimaryKeyStore(Config(), schemaInfo)

    val pkStoreWorkflow =
      new PkStoreWorkflow(pkStore, schemaInfo)

    val fkValue: String = "fkValue"

    val row: Row = Array(fkValue)

    val rows: Vector[Row] = Vector(row)

    // Add the PK to the pkStore, noting that we have NOT yet fetched children
    val pkAddRequest1 = OriginDbResult(table, rows, None, fetchChildren = false)
    val pkAddResult1 = pkStoreWorkflow.add(pkAddRequest1)
    assert(pkAddResult1 === PksAdded(table, rows, Vector.empty, None))

    // Query whether the PK is in the pkStore given that we are only interested in parent records
    // The return value should be true, meaning that yes it's in the pkStore at least as far as having fetched its parent records
    assert(pkStore.alreadySeen(table, fkValue) === true)

    // Now we add the the PK to the pkStore noting that we will fetch children for it
    // The fact that it was already in the PK store for having its parents fetched means that
    // It will only appear in the collection of rows still needing children processing
    // It will not appear in the collection of rows needing parents (and therefore will not be added duplicate to the target db either)
    val pkAddRequest2 = OriginDbResult(table, rows, None, fetchChildren = true)
    val pkAddResult2 = pkStoreWorkflow.add(pkAddRequest2)
    assert(pkAddResult2 === PksAdded(table, Vector.empty, rows, None))

    // Do the same query as before
    // Query whether the PK is in the pkStore given that we are only interested in parent records
    // The return value should be true, meaning that yes it's in the pkStore at least as far as having fetched its parent records
    // (This query only returns info about having fetched parent records, but it should remain true even though the last thing we did was fetch children)
    assert(pkStore.alreadySeen(table, fkValue) === true)
  }

  test("PkStoreWorkflow is conscious of fetchChildren part2") {
    val table: Table =
      new Table(
        schema = "public",
        name = "users",
        hasSqlServerAutoIncrement = true,
        storePks = true
      )

    val primaryKeyColumn: Column =
      new Column(
        table = table,
        name = null,
        ordinalPosition = 0,
        jdbcType = null,
        typeName = null
      )

    val schemaInfo: SchemaInfo =
      new SchemaInfo(
        tablesByName = Map.empty,
        colsByTableOrdered = Map.empty,
        pksByTableOrdered = Map(table -> Vector(primaryKeyColumn)),
        fksOrdered = Array.empty,
        fksFromTable = Map.empty,
        fksToTable = Map.empty,
        dbVendor = null
      )

    val pkStore: PrimaryKeyStore =
      PrimaryKeyStoreFactory.buildPrimaryKeyStore(Config(), schemaInfo)

    val pkStoreWorkflow: PkStoreWorkflow =
      new PkStoreWorkflow(pkStore, schemaInfo)

    val fkValue: String = "fkValue"
    val row: Row = Array(fkValue)
    val rows: Vector[Row] = Vector(row)

    // Add the PK to the pkStore, noting that we have NOT yet fetched children
    val pkAddRequest1 = OriginDbResult(table, rows, viaTableOpt = None, fetchChildren = true)
    val actual = pkStoreWorkflow.add(pkAddRequest1)
    val expected: PksAdded = PksAdded(
      table = table,
      rowsNeedingParentTasks = rows,
      rowsNeedingChildTasks = rows,
      viaTableOpt = None
    )
    assert(actual === expected)
  }
}

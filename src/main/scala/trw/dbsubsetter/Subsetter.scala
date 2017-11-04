package trw.dbsubsetter

import java.sql.DriverManager

import scala.collection.mutable

object Subsetter extends App {
  val schemas = Set("public", "audit")
  val originConnectionString = "jdbc:postgresql://localhost:5450/db_subsetter_origin?user=postgres"
  val targetConnectionString = "jdbc:postgresql://localhost:5451/db_subsetter_target?user=postgres"
  val startingSchemaName = "public"
  val startingTableName = "students"
  val startingWhereClause = "current_school_id_cache % 1000 = 0"

  val originConn = DriverManager.getConnection(originConnectionString)
  originConn.setReadOnly(true)
  val targetConn = DriverManager.getConnection(targetConnectionString)

  // DB Schema Info
  val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(originConn, schemas)

  // Queue of items/tasks still to be processed
  val processingQueue = mutable.Queue.empty[Task]

  // In-memory storage for primary key values
  val pkStore: PrimaryKeyStore = schemaInfo.tablesByName.map { case (_, table) =>
    table -> mutable.HashSet.empty[Vector[AnyRef]]
  }

  val preparedStatements = PreparedStatementMaker.prepareStatements(originConn, schemaInfo)

  // Starting query (a bit of duplication with the PreparedStatementMaker)
  // Still need to add these values to the pkStore?
  val startingTable = schemaInfo.tablesByName((startingSchemaName, startingTableName))
  val startingCols = {
    schemaInfo.pksByTable(startingTable).columns ++
      schemaInfo.fksToTable(startingTable).flatMap(_.columns.map { case (_, to) => to }) ++
      schemaInfo.fksFromTable(startingTable).flatMap(_.columns.map { case (from, _) => from })
  }
  val startingQuery =
    s"""select ${startingCols.map(_.name)}
       |from ${startingTable.schema}.${startingTable.name}
       |where $startingWhereClause""".stripMargin

  val startingRows = DbAccess.getRows(originConn, startingQuery, startingCols)

  val startingParentTasks = for {
    row <- startingRows
    fk <- schemaInfo.fksFromTable(startingTable)
    cols = fk.columns.map { case (from, _) => from }
    values = cols.map(row)
  } yield Task(fk.toTable, fk, values, false)

  val startingChildTasks = for {
    row <- startingRows
    fk <- schemaInfo.fksToTable(startingTable)
    cols = fk.columns.map { case (_, to) => to }
    values = cols.map(row)
  } yield Task(fk.fromTable, fk, values, true)

  processingQueue.enqueue(startingParentTasks: _*)
  processingQueue.enqueue(startingChildTasks: _*)

  // Continuously read tasks off of the processing queue, adding new tasks to it as necessary
  while (processingQueue.nonEmpty) {
    val newTasks = Processor.process(processingQueue.dequeue(), schemaInfo, originConn, pkStore, preparedStatements)
    processingQueue.enqueue(newTasks: _*)
  }

  // Once the queue has been completely emptied out, this means the pkStore contains all the primary keys we need
  // Copy the data matching these primary keys from the origin db to the target db
  // Consider streaming pks in batches of ~ 10,000 in order to limit memory usage for very large tables
  pkStore.foreach { case (table, pks) =>
    if (pks.nonEmpty) {
      Copier.copyToTargetDB(
        originConn,
        targetConn,
        schemaInfo.pksByTable(table),
        pks
      )
    }
  }
}

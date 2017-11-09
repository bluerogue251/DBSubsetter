package trw.dbsubsetter

import java.sql.DriverManager

import scala.collection.mutable

object SubsettingOrchestrator {
  def doSubset(config: Config): Unit = {
    val startingTime = System.nanoTime()

    val originConn = DriverManager.getConnection(config.originDbConnectionString)
    originConn.setReadOnly(true)
    val targetConn = DriverManager.getConnection(config.targetDbConnectionString)

    // DB Schema Info
    val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(originConn, config.schemas)

    // Queue of items/tasks still to be processed
    val processingQueue = mutable.Queue.empty[Task]

    // In-memory storage for primary key values
    val pkStore: PrimaryKeyStore = schemaInfo.tablesByName.map { case (_, table) =>
      table -> mutable.HashSet.empty[Vector[AnyRef]]
    }

    val preparedStatements = SqlStatementMaker.prepareStatements(originConn, schemaInfo)

    config.baseQueries.foreach { case ((schemaName, tableName), whereClause) =>
      // Starting query (a bit of duplication with the PreparedStatementMaker)
      // Still need to add these values to the pkStore?
      val table = schemaInfo.tablesByName((schemaName, tableName))
      val (query, selectCols) = SqlStatementMaker.makeSqlString(table, whereClause, schemaInfo, includeChildren = true)
      val startingRows = DbAccess.getRows(originConn, query, selectCols)
      val tasks = RowsToTasksConverter.convert(table, startingRows, schemaInfo, fetchChildren = true)
      processingQueue.enqueue(tasks: _*)
    }

    // Continuously read tasks off of the processing queue, adding new tasks to it as necessary
    while (processingQueue.nonEmpty) {
      val newTasks = Processor.process(processingQueue.dequeue(), schemaInfo, originConn, pkStore, preparedStatements)
      processingQueue.enqueue(newTasks: _*)
    }

    val doneProcessingTime = System.nanoTime()
    println(s"Done Processing! Took ${(doneProcessingTime - startingTime) / 1000000000} seconds")

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

    val endingTime = System.nanoTime()
    println(s"Done copying! Took ${(endingTime - doneProcessingTime) / 1000000000} seconds")
    println(s"Success! Took ${(endingTime - startingTime) / 1000000000} seconds")
  }

}

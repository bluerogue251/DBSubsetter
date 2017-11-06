package trw.dbsubsetter

import java.sql.DriverManager

import scala.collection.mutable

object Subsetter extends App {
  val startingTime = System.nanoTime()

  // TODO avoid using `isEmpty` and `get` methods on `Option`
  val configOpt: Option[Config] = CommandLineParser.parser.parse(args, Config())
  if (configOpt.isEmpty) System.exit(1)
  val config = configOpt.get

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

  val preparedStatements = PreparedStatementMaker.prepareStatements(originConn, schemaInfo)

  config.baseQueries.foreach { case ((schemaName, tableName), whereClause) =>
    // Starting query (a bit of duplication with the PreparedStatementMaker)
    // Still need to add these values to the pkStore?
    val startingTable = schemaInfo.tablesByName((schemaName, tableName))
    val startingCols = {
      schemaInfo.pksByTable(startingTable).columns ++
        schemaInfo.fksToTable(startingTable).flatMap(_.toCols) ++
        schemaInfo.fksFromTable(startingTable).flatMap(_.fromCols)
    }
    val startingQuery =
      s"""select ${startingCols.map(_.name).mkString(", ")}
         |from ${startingTable.schema}.${startingTable.name}
         |where $whereClause""".stripMargin

    val startingRows = DbAccess.getRows(originConn, startingQuery, startingCols)

    val startingParentTasks = for {
      row <- startingRows
      fk <- schemaInfo.fksFromTable(startingTable)
      cols = fk.fromCols
      values = cols.map(row)
    } yield Task(fk.toTable, fk, values, false)

    val startingChildTasks = for {
      row <- startingRows
      fk <- schemaInfo.fksToTable(startingTable)
      cols = fk.toCols
      values = cols.map(row)
    } yield Task(fk.fromTable, fk, values, true)

    processingQueue.enqueue(startingParentTasks: _*)
    processingQueue.enqueue(startingChildTasks: _*)
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

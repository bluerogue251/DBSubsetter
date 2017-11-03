package trw.dbsubsetter

import java.sql.DriverManager

import scala.collection.mutable

object Subsetter extends App {
  val schemas = Set("public", "audit")
  val originConnectionString = "jdbc:postgresql://localhost:5450/db_subsetter_origin?user=postgres"
  val targetConnectionString = "jdbc:postgresql://localhost:5451/db_subsetter_target?user=postgres"
  val startingSchema = "public"
  val startingTable = "students"
  val startingWhereClause = "current_school_id_cache % 1000 = 0"

  val originConn = DriverManager.getConnection(originConnectionString)
  originConn.setReadOnly(true)
  val targetConn = DriverManager.getConnection(targetConnectionString)

  // DB Schema Info
  val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(originConn, schemas)

  // Queue of items/tasks still to be processed
  val processingQueue = mutable.Queue.empty[Task]

  // In-memory storage for primary key values
  val pkStore: PrimaryKeyStore = schemaInfo.tables.map { table =>
    (table.schema, table.name) -> mutable.HashSet.empty[Vector[AnyRef]]
  }.toMap

  // Queue up the task to start subsetting with based on the user-supplied schema, table, and where-clause
  val startingTask = Task(startingSchema, startingTable, startingWhereClause, true)
  processingQueue.enqueue(startingTask)

  // Continuously read tasks off of the processing queue, adding new tasks to it as necessary
  while (processingQueue.nonEmpty) {
    val newTasks = Processor.process(processingQueue.dequeue(), schemaInfo, originConn, pkStore)
    processingQueue.enqueue(newTasks: _*)
  }

  // Once the queue has been completely emptied out, this means the pkStore contains all the primary keys we need
  // Copy the data matching these primary keys from the origin db to the target db
  // Consider streaming pks in batches of ~ 10,000 in order to limit memory usage for very large tables
  pkStore.foreach { case ((schema, table), pks) =>
    if (pks.nonEmpty) {
      Copier.copyToTargetDB(
        originConn,
        targetConn,
        schemaInfo.pksByTable(schema, table),
        pks
      )
    }
  }
}

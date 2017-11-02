package trw.dbsubsetter

import java.sql.DriverManager

import scala.collection.mutable

object Subsetter extends App {
  val schemas = Set("public", "audit")
  val connectionString = "jdbc:postgresql://localhost:5450/db_subsetter_origin?user=postgres"
  val startingSchema = "public"
  val startingTable = "students"
  val startingWhereClause = "random() < 0.001"

  val conn = DriverManager.getConnection(connectionString)
  conn.setReadOnly(true)

  // DB Schema Info
  val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(conn, schemas)

  // Queue of items/tasks still to be processed/worked on
  val processingQueue = mutable.Queue.empty[Task]

  // In-memory storage for primary key values
  val pkStore: PrimaryKeyStore = schemaInfo.tables.map { table =>
    (table.schema, table.name) -> mutable.HashSet.empty[Vector[AnyRef]]
  }.toMap

  val startingTask = Task(startingSchema, startingTable, startingWhereClause, true)
  processingQueue.enqueue(startingTask)

  while (processingQueue.nonEmpty) {
    pkStore.foreach { case ((schemaName, tableName), hashSet) =>
      println(s"$schemaName.$tableName: ${hashSet.size}")
    }
    println("*" * 60)

    val newTasks = Processor.process(processingQueue.dequeue(), schemaInfo, conn, pkStore)
    processingQueue.enqueue(newTasks: _*)
  }
}


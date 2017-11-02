package trw.dbsubsetter

import java.sql.DriverManager

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Subsetter extends App {
  val schemas = Set("public", "audit")
  val connectionString = "jdbc:postgresql://localhost:5450/db_subsetter_origin?user=postgres"
  val startingSchema = "public"
  val startingTable = "students"
  val startingWhereClause = "random() < 0.001"

  val dbConnection = DriverManager.getConnection(connectionString)
  dbConnection.setReadOnly(true)

  // DB Schema Info
  val sch = DbSchemaInfoRetrieval.getSchemaInfo(dbConnection, schemas)

  // Queue of items/tasks still to be processed/worked on
  val processingQueue = mutable.Queue.empty[Task]

  // In-memory storage for primary key values
  val primaryKeyStore: Map[(SchemaName, TableName), mutable.HashSet[Vector[AnyRef]]] = {
    sch.tables.map(table => (table.schema, table.name) -> mutable.HashSet.empty[Vector[AnyRef]]).toMap
  }

  def process(): Unit = {
    while (processingQueue.nonEmpty) {
      val Task(schema, table, whereClause, fetchChildren, generation) = processingQueue.dequeue()
      // Figure out which columns we need to include in the SQL `SELECT` statement
      // So that we don't select any more data than is absolutely necessary
      val pk = sch.pksByTable((schema, table))
      val parentFks = sch.fksFromTable((schema, table))
      val childFks = sch.fksToTable((schema, table))
      val parentFkColsToSelect = parentFks.flatMap(_.columns).map { case (fromCol, _) => fromCol }
      val childFkColsToSelect = if (fetchChildren) childFks.flatMap(_.columns).map { case (_, toCol) => toCol } else Set.empty
      val columnsToSelect: Seq[Column] = pk ++ parentFkColsToSelect ++ childFkColsToSelect

      // Build and execute the SQL statement to select the data matching the where clause
      val query =
        s"""select ${columnsToSelect.map(_.name).mkString(", ")}
           | from $schema.$table
           | where $whereClause
           | """.stripMargin
      val resultSet = dbConnection.createStatement().executeQuery(query)

      // Put the result in a collection of Maps from column names to values, each element in the collection is a row of the result
      // Could we be more efficient by doing this by index rather than by column name?
      val tmpResult = ArrayBuffer.empty[Map[ColumnName, AnyRef]]
      while (resultSet.next()) {
        tmpResult += columnsToSelect.map(col => col.name -> resultSet.getObject(col.name)).toMap
      }

      // Find out which rows are "new" in the sense of having not yet been processed by us
      // Add the primary key of each of the "new" rows to the primaryKeyStore.
      val newRows = tmpResult.filter { row =>
        primaryKeyStore((schema, table)).add(pk.map(k => row(k.name)))
      }

      // For each "new" row, call `process` method recursively on its parents and children
      newRows.foreach { row =>
        parentFks.foreach { pfk =>
          val whereClause = pfk.columns.flatMap { case (fromCol, toCol) =>
            Option(row(fromCol.name)).map(fromColValue => s"${toCol.name} = '$fromColValue'")
          }.mkString(" and ")
          if (whereClause.nonEmpty) processingQueue.enqueue(Task(pfk.toSchema, pfk.toTable, whereClause, false, generation + 1))
        }

        if (fetchChildren) {
          childFks.foreach { cfk =>
            val whereClause = cfk.columns.flatMap { case (fromCol, toCol) =>
              Option(row(toCol.name)).map(toColValue => s"${fromCol.name} = '$toColValue'")
            }.mkString(" and ")
            if (whereClause.nonEmpty) processingQueue.enqueue(Task(cfk.fromSchema, cfk.fromTable, whereClause, true, generation + 1))
          }
        }
      }

      // Print Debug info about what primary keys we have so far
      primaryKeyStore.foreach { case ((schemaName, tableName), hashSet) =>
        println(s"$schemaName.$tableName: ${hashSet.size}")
      }
    }
  }

  processingQueue.enqueue(Task(startingSchema, startingTable, startingWhereClause, true, 0))
  process()
}

case class Task(schema: SchemaName, table: TableName, whereClause: WhereClause, fetchChildren: Boolean, generation: Int)
package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, PrimaryKeyValue, Row, SchemaInfo, Table}
import trw.dbsubsetter.primarykeystore.{PrimaryKeyStore, PrimaryKeyStoreFactory}
import trw.dbsubsetter.taskqueue.{TaskQueue, TaskQueueFactory}
import trw.dbsubsetter.workflow._

object ApplicationSingleThreaded {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery]): Unit = {
    // Set up workflow objects
    val dbAccessFactory = new DbAccessFactory(config, schemaInfo)
    val originDbWorkflow = new OriginDbWorkflow(config, schemaInfo, dbAccessFactory)
    val targetDbWorkflow = new TargetDbWorkflow(dbAccessFactory)
    val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.buildPrimaryKeyStore(config, schemaInfo)
    val pkWorkflow: PkStoreWorkflow = new PkStoreWorkflow(pkStore, schemaInfo)
    val fkTaskCreationWorkflow: FkTaskCreationWorkflow = new FkTaskCreationWorkflow(schemaInfo)

    val pkValueExtractionFunctions: Map[Table, Row => PrimaryKeyValue] =
      buildPkValueExtractionFunctions(schemaInfo)

    // Set up task queue
    val taskQueue: TaskQueue = TaskQueueFactory.buildTaskQueue(config)
    taskQueue.enqueue(baseQueries)

    // Run task queue until empty
    while (taskQueue.nonEmpty) {
      val taskOpt: Option[OriginDbRequest] = taskQueue.dequeue() match {
        case t: FetchParentTask if FkTaskPreCheck.shouldPrecheck(t) =>
          if (pkStore.alreadySeen(t.parentTable, t.fkValueFromChild)) None else Some(t)
        case t =>
          Some(t)
      }
      taskOpt.foreach { task =>
        val dbResult = originDbWorkflow.process(task)
        val pksAdded = pkWorkflow.add(dbResult)
        val extractPkValue: Row => PrimaryKeyValue = pkValueExtractionFunctions(pksAdded.table)
        val pkValues: Seq[PrimaryKeyValue] = pksAdded.rowsNeedingParentTasks.map(extractPkValue)
        val dataCopyTask = new DataCopyTask(pksAdded.table, pkValues)
        targetDbWorkflow.process(dataCopyTask)
        val newTasks = fkTaskCreationWorkflow.createFkTasks(pksAdded)
        newTasks.taskInfo.foreach { case ((foreignKey, fetchChildren), fkValues) =>
          val tasks = fkValues.map { fkValue =>
            RawTaskToForeignKeyTaskMapper.map(foreignKey, fetchChildren, fkValue)
          }
          taskQueue.enqueue(tasks)
        }
      }
    }

    // Ensure all SQL connections get closed
    dbAccessFactory.closeAllConnections()
  }

  // TODO consider deduplication with similar logic in PkStoreWorkflow and DataCopyQueueImpl
  private def buildPkValueExtractionFunctions(schemaInfo: SchemaInfo): Map[Table, Row => PrimaryKeyValue] = {
    schemaInfo.pksByTableOrdered.map { case (table, pkColumns) =>
      val primaryKeyColumnOrdinals: Vector[Int] = pkColumns.map(_.ordinalPosition)
      val primaryKeyExtractionFunction: Row => PrimaryKeyValue = row => {
        val individualColumnValues: Seq[Any] = primaryKeyColumnOrdinals.map(row)
        new PrimaryKeyValue(individualColumnValues)
      }
      table -> primaryKeyExtractionFunction
    }
  }
}

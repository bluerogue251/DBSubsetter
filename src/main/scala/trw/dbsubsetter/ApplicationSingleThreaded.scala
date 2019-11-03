package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, Keys, PrimaryKeyValue, SchemaInfo, Table}
import trw.dbsubsetter.keyextraction.KeyExtractionUtil
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

    val pkValueExtractionFunctions: Map[Table, Keys => PrimaryKeyValue] =
      KeyExtractionUtil.pkExtractionFunctions(schemaInfo)

    // Set up task queue
    val taskQueue: TaskQueue = TaskQueueFactory.buildTaskQueue(config)
    taskQueue.enqueue(baseQueries)

    // Run task queue until empty
    while (taskQueue.nonEmpty) {
      val taskOpt: Option[OriginDbRequest] =
        taskQueue.dequeue() match {
          case t: FetchParentTask if FkTaskPreCheck.shouldPrecheck(t) =>
            if (pkStore.alreadySeen(t.parentTable, new PrimaryKeyValue(t.fkValueFromChild.individualColumnValues))) None else Some(t)
          case t =>
            Some(t)
        }

      taskOpt.foreach { task =>
        val dbResult: OriginDbResult = originDbWorkflow.process(task)

        val pksAdded: PksAdded = pkWorkflow.add(dbResult)

        val pkValueExtractionFunction: Keys => PrimaryKeyValue = pkValueExtractionFunctions(pksAdded.table)
        val pkValues: Seq[PrimaryKeyValue] = pksAdded.rowsNeedingParentTasks.map(pkValueExtractionFunction)

        val dataCopyTask: DataCopyTask = new DataCopyTask(pksAdded.table, pkValues)
        targetDbWorkflow.process(dataCopyTask)

        val newForeignKeyTasks: IndexedSeq[ForeignKeyTask] = fkTaskCreationWorkflow.createFkTasks(pksAdded)
        taskQueue.enqueue(newForeignKeyTasks)
      }
    }

    // Ensure all SQL connections get closed
    dbAccessFactory.closeAllConnections()
  }
}

package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.datacopy.DataCopyWorkflowFactory
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.datacopyqueue.DataCopyQueueFactory
import trw.dbsubsetter.db.DbAccessFactory
import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.db.Table
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueueFactory
import trw.dbsubsetter.primarykeystore.PrimaryKeyStore
import trw.dbsubsetter.primarykeystore.PrimaryKeyStoreFactory
import trw.dbsubsetter.workflow._

class ApplicationSingleThreaded(config: Config, schemaInfo: SchemaInfo, baseQueries: Seq[BaseQuery]) {

  private[this] val dbAccessFactory =
    new DbAccessFactory(config, schemaInfo)

  private[this] val originDbWorkflow =
    new OriginDbWorkflow(config, schemaInfo, dbAccessFactory)

  private[this] val dataCopyWorkflow =
    DataCopyWorkflowFactory.build(dbAccessFactory, schemaInfo)

  private[this] val pkStore: PrimaryKeyStore =
    PrimaryKeyStoreFactory.buildPrimaryKeyStore(config, schemaInfo)

  private[this] val pkWorkflow: PkStoreWorkflow =
    new PkStoreWorkflow(pkStore, schemaInfo)

  private[this] val fkTaskCreationWorkflow: FkTaskCreationWorkflow =
    new FkTaskCreationWorkflow(schemaInfo)

  private[this] val fkTaskQueue: ForeignKeyTaskQueue =
    ForeignKeyTaskQueueFactory.build(config, schemaInfo)

  private[this] val dataCopyQueue: DataCopyQueue =
    DataCopyQueueFactory.buildDataCopyQueue(config, schemaInfo)

  def run(): Unit = {
    // Handle all key calculation from base queries
    baseQueries.foreach(handleKeyCalculationTask)

    // Handle all key calculation foreign key tasks
    while (!fkTaskQueue.isEmpty()) {
      val fkTask: ForeignKeyTask = fkTaskQueue.dequeue().get
      handleFkTask(fkTask)
    }

    // Populate target db with data
    while (!dataCopyQueue.isEmpty()) {
      val dataCopyTask: DataCopyTask = dataCopyQueue.dequeue().get
      dataCopyWorkflow.process(dataCopyTask)
    }

    // Ensure all SQL connections get closed
    dbAccessFactory.closeAllConnections()
  }

  private def handleFkTask(task: ForeignKeyTask): Unit = {
    val isDuplicate: Boolean =
      task match {
        case fetchParentTask: FetchParentTask if FkTaskPreCheck.shouldPrecheck(fetchParentTask) =>
          val tableToCheck: Table = fetchParentTask.fk.toTable
          val primaryKeyValueToCheck: PrimaryKeyValue =
            new PrimaryKeyValue(fetchParentTask.fkValueFromChild.individualColumnValues)
          pkStore.alreadySeen(tableToCheck, primaryKeyValueToCheck)
        case _ => false
      }

    if (!isDuplicate) {
      handleKeyCalculationTask(task)
    }
  }

  private def handleKeyCalculationTask(task: OriginDbRequest): Unit = {
    // Query the origin database
    val dbResult: OriginDbResult = originDbWorkflow.process(task)

    // Calculate which rows we've seen already
    val pksAdded: PksAdded = pkWorkflow.add(dbResult)

    // Queue up the newly seen rows to be copied into the target database
    dataCopyQueue.enqueue(pksAdded)

    // Queue up any new tasks resulting from this stage
    fkTaskCreationWorkflow
      .createFkTasks(pksAdded)
      .foreach(fkTaskQueue.enqueue)
  }
}

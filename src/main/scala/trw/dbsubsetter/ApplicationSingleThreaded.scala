package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.datacopy.DataCopierFactoryImpl
import trw.dbsubsetter.datacopyqueue.{DataCopyQueue, DataCopyQueueFactory}
import trw.dbsubsetter.db.{DbAccessFactory, PrimaryKeyValue, SchemaInfo, Table}
import trw.dbsubsetter.fktaskqueue.{ForeignKeyTaskQueue, ForeignKeyTaskQueueFactory}
import trw.dbsubsetter.primarykeystore.{PrimaryKeyStore, PrimaryKeyStoreFactory}
import trw.dbsubsetter.workflow._

class ApplicationSingleThreaded(config: Config, schemaInfo: SchemaInfo, baseQueries: Set[BaseQuery]) {

  private[this] val dbAccessFactory =
    new DbAccessFactory(config, schemaInfo)

  private[this] val originDbWorkflow =
    new OriginDbWorkflow(config, schemaInfo, dbAccessFactory)

  private[this] val dataCopier =
    new DataCopierFactoryImpl(dbAccessFactory, schemaInfo).build()

  private[this] val pkStore: PrimaryKeyStore =
    PrimaryKeyStoreFactory.buildPrimaryKeyStore(schemaInfo)

  private[this] val pkWorkflow: PkStoreWorkflow =
    new PkStoreWorkflow(pkStore, schemaInfo)

  private[this] val fkTaskGenerator: FkTaskGenerator =
    new FkTaskGenerator(schemaInfo)

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
      dataCopier.runTask(dataCopyTask)
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
    fkTaskGenerator
      .generateFrom(pksAdded)
      .foreach(fkTaskQueue.enqueue)
  }
}

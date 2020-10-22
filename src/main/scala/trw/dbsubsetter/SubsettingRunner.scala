package trw.dbsubsetter

import trw.dbsubsetter.basequery.{BaseQueryPhase, BaseQueryPhaseImpl}
import trw.dbsubsetter.config.{BaseQuery, Config}
import trw.dbsubsetter.datacopy._
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.fkcalc._
import trw.dbsubsetter.fktaskqueue.{ForeignKeyTaskQueue, ForeignKeyTaskQueueFactory}
import trw.dbsubsetter.keyingestion.{KeyIngester, KeyIngesterImpl}
import trw.dbsubsetter.pkstore.{PkStoreWorkflow, PrimaryKeyStore, PrimaryKeyStoreFactory}

object SubsettingRunner {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Set[BaseQuery]): Unit = {
    val dbAccessFactory: DbAccessFactory = new DbAccessFactory(config, schemaInfo)
    val dataCopyQueue: DataCopyQueue = DataCopyQueueFactory.buildDataCopyQueue(config, schemaInfo)

    runKeyCalculationPhase(
      config,
      baseQueries,
      dbAccessFactory,
      schemaInfo,
      dataCopyQueue
    )

    runDataCopyPhase(
      dbAccessFactory,
      schemaInfo,
      config.dataCopyDbConnectionCount,
      dataCopyQueue
    )
  }

  def runKeyCalculationPhase(
      config: Config,
      baseQueries: Set[BaseQuery],
      dbAccessFactory: DbAccessFactory,
      schemaInfo: SchemaInfo,
      dataCopyQueue: DataCopyQueue
  ): Unit = {
    val fkTaskGenerator: FkTaskGenerator = new FkTaskGenerator(schemaInfo)
    val fkTaskQueue: ForeignKeyTaskQueue = ForeignKeyTaskQueueFactory.build(config, schemaInfo)

    // Ensure all primary key store things stay as local vars so that they are JVM Garbage Collected Earlier
    val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.buildPrimaryKeyStore(schemaInfo)
    val pkStoreWorkflow: PkStoreWorkflow = new PkStoreWorkflow(pkStore, schemaInfo)
    val keyIngester: KeyIngester = new KeyIngesterImpl(pkStoreWorkflow, dataCopyQueue, fkTaskGenerator, fkTaskQueue)

    runBaseQueryPhase(baseQueries, dbAccessFactory, keyIngester)

    if (fkTaskQueue.nonEmpty()) {
      runFkCalculationPhase(config, dbAccessFactory, pkStoreWorkflow, fkTaskQueue, keyIngester)
    }
  }

  private def runBaseQueryPhase(
      baseQueries: Set[BaseQuery],
      dbAccessFactory: DbAccessFactory,
      keyIngester: KeyIngester
  ): Unit = {
    val phase: BaseQueryPhase =
      new BaseQueryPhaseImpl(
        baseQueries,
        dbAccessFactory.buildOriginDbAccess(),
        keyIngester
      )
    phase.runPhase()
    dbAccessFactory.closeAllConnections()
  }

  private def runFkCalculationPhase(
      config: Config,
      dbAccessFactory: DbAccessFactory,
      pkStoreWorkflow: PkStoreWorkflow,
      fkTaskQueue: ForeignKeyTaskQueue,
      keyIngester: KeyIngester
  ): Unit = {
    val taskHandlers: Seq[ForeignKeyTaskHandler] =
      (1 to config.keyCalculationDbConnectionCount)
        .map(_ => new ForeignKeyTaskHandler(dbAccessFactory))

    val phase: ForeignKeyCalculationPhase =
      new ForeignKeyCalculationPhaseImpl(
        fkTaskQueue,
        taskHandlers,
        pkStoreWorkflow,
        keyIngester
      )

    phase.runPhase()
    dbAccessFactory.closeAllConnections()
  }

  private def runDataCopyPhase(
      dbAccessFactory: DbAccessFactory,
      schemaInfo: SchemaInfo,
      dataCopyDbConnectionCount: Int,
      dataCopyQueue: DataCopyQueue
  ): Unit = {
    val copierFactory: DataCopierFactory =
      new DataCopierFactoryImpl(dbAccessFactory, schemaInfo)

    val copiers: Seq[DataCopier] =
      (1 to dataCopyDbConnectionCount).map(_ => copierFactory.build())

    val phase: DataCopyPhase =
      new DataCopyPhaseImpl(dataCopyQueue, copiers)

    phase.runPhase()
    dbAccessFactory.closeAllConnections()
  }
}

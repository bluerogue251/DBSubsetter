package trw.dbsubsetter

import java.nio.file.{Files, Path, Paths}

import trw.dbsubsetter.basequery.{BaseQueryPhase, BaseQueryPhaseImpl}
import trw.dbsubsetter.config.{BaseQuery, Config}
import trw.dbsubsetter.datacopy.{DataCopyPhase, DataCopyQueue}
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.fkcalc._
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.keyingestion.{KeyIngester, KeyIngesterImpl}
import trw.dbsubsetter.pkstore.{PkStoreWorkflow, PrimaryKeyStore}

object SubsettingRunner {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Set[BaseQuery]): Unit = {
    val dbAccessFactory: DbAccessFactory = new DbAccessFactory(config, schemaInfo)

    val baseStorageDirectory: Path = config.storageDirectory.getOrElse(Files.createTempDirectory("DBSubsetter-"))
    val keyCalculationQueueStorageDirectory = Paths.get(baseStorageDirectory.toString, "key-calculation")
    val dataCopyQueueStorageDirectory = Paths.get(baseStorageDirectory.toString, "data-copy")

    val dataCopyQueue: DataCopyQueue = DataCopyQueue.from(dataCopyQueueStorageDirectory, schemaInfo)

    runKeyCalculationPhase(
      config.keyCalculationDbConnectionCount,
      keyCalculationQueueStorageDirectory,
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
      parallelism: Int,
      queueStorageDir: Path,
      baseQueries: Set[BaseQuery],
      dbAccessFactory: DbAccessFactory,
      schemaInfo: SchemaInfo,
      dataCopyQueue: DataCopyQueue
  ): Unit = {
    val fkTaskGenerator: FkTaskGenerator = new FkTaskGenerator(schemaInfo)
    val fkTaskQueue: ForeignKeyTaskQueue = ForeignKeyTaskQueue.from(queueStorageDir, schemaInfo)

    // Ensure all primary key store things stay as local vars so that they are JVM Garbage Collected Earlier
    val pkStore: PrimaryKeyStore = PrimaryKeyStore.from(schemaInfo)
    val pkStoreWorkflow: PkStoreWorkflow = new PkStoreWorkflow(pkStore, schemaInfo)
    val keyIngester: KeyIngester = new KeyIngesterImpl(pkStoreWorkflow, dataCopyQueue, fkTaskGenerator, fkTaskQueue)

    runBaseQueryPhase(baseQueries, dbAccessFactory, keyIngester)

    if (fkTaskQueue.nonEmpty()) {
      runFkCalculationPhase(parallelism, dbAccessFactory, pkStoreWorkflow, fkTaskQueue, keyIngester)
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
      parallelism: Int,
      dbAccessFactory: DbAccessFactory,
      pkStoreWorkflow: PkStoreWorkflow,
      fkTaskQueue: ForeignKeyTaskQueue,
      keyIngester: KeyIngester
  ): Unit = {
    val taskHandlers: Seq[ForeignKeyTaskHandler] =
      (1 to parallelism)
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
    val phase: DataCopyPhase =
      DataCopyPhase.from(
        dataCopyDbConnectionCount,
        dbAccessFactory,
        schemaInfo,
        dataCopyQueue
      )

    phase.runPhase()
    dbAccessFactory.closeAllConnections()
  }
}

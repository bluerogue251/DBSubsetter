package trw.dbsubsetter

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import trw.dbsubsetter.akkastreams.{KeyQueryGraphFactory, PkStoreActor}
import trw.dbsubsetter.basequery.{BaseQueryPhase, BaseQueryPhaseImpl}
import trw.dbsubsetter.config.{BaseQuery, Config}
import trw.dbsubsetter.datacopy._
import trw.dbsubsetter.datacopyqueue.{DataCopyQueue, DataCopyQueueFactory}
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.fktaskqueue.{ForeignKeyTaskQueue, ForeignKeyTaskQueueFactory}
import trw.dbsubsetter.keyingestion.{KeyIngester, KeyIngesterImpl}
import trw.dbsubsetter.primarykeystore.{PrimaryKeyStore, PrimaryKeyStoreFactory}
import trw.dbsubsetter.workflow._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object ApplicationAkkaStreams {
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
      runFkCalculationPhase(config, dbAccessFactory, dataCopyQueue, fkTaskGenerator, fkTaskQueue, pkStoreWorkflow)
    }
  }

  private def runBaseQueryPhase(
      baseQueries: Set[BaseQuery],
      dbAccessFactory: DbAccessFactory,
      keyIngester: KeyIngester
  ): Unit = {
    // Run Base Query Phase
    val baseQueryPhase: BaseQueryPhase =
      new BaseQueryPhaseImpl(
        baseQueries,
        dbAccessFactory.buildOriginDbAccess(),
        keyIngester
      )
    baseQueryPhase.runPhase()
    dbAccessFactory.closeAllConnections()
  }

  private def runFkCalculationPhase(
      config: Config,
      dbAccessFactory: DbAccessFactory,
      dataCopyQueue: DataCopyQueue,
      fkTaskGenerator: FkTaskGenerator,
      fkTaskQueue: ForeignKeyTaskQueue,
      pkStoreWorkflow: PkStoreWorkflow
  ): Unit = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher
    val pkStoreActorRef: ActorRef = system.actorOf(PkStoreActor.props(pkStoreWorkflow))

    val keyQueryPhase: Future[Done] =
      KeyQueryGraphFactory
        .build(
          config,
          pkStoreActorRef,
          dbAccessFactory,
          fkTaskGenerator,
          fkTaskQueue,
          dataCopyQueue
        )
        .run()

    // Wait for the key query phase to complete. Use `result` rather than `ready` to ensure an exception is thrown on failure.
    Await.result(keyQueryPhase, Duration.Inf)
    system.terminate()
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

    val dataCopyPhase: DataCopyPhase =
      new DataCopyPhaseImpl(dataCopyQueue, copiers)

    dataCopyPhase.runPhase()
    dbAccessFactory.closeAllConnections()
  }
}

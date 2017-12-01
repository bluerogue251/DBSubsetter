package e2e

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.dbio.{DBIOAction, Effect}
import slick.jdbc.JdbcBackend
import slick.lifted.{AbstractTable, TableQuery}
import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.SchemaInfoRetrieval
import trw.dbsubsetter.workflow.BaseQueries
import trw.dbsubsetter.{ApplicationAkkaStreams, ApplicationSingleThreaded}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class AbstractEndToEndTest extends FunSuite with BeforeAndAfterAll {
  //
  // The following need to be overridden
  //
  protected val profile: slick.jdbc.JdbcProfile

  protected def originPort: Int

  protected def makeConnStr(port: Int): String

  protected def programArgs: Array[String]

  protected def createOriginDb(): Unit

  protected def setupTargetDbs(): Unit

  protected def postSubset(): Unit

  protected def setupDDL(): Unit

  protected def setupDML(): Unit

  //
  // The following is generic enough that it generally does not need to be overridden
  //
  var originDb: JdbcBackend#DatabaseDef = _
  var targetDbSt: JdbcBackend#DatabaseDef = _
  var targetDbAs: JdbcBackend#DatabaseDef = _
  lazy val targetSingleThreadedPort: Int = originPort + 1
  lazy val targetAkkaStreamsPort: Int = originPort + 2
  lazy val targetSingleThreadedConnString: String = makeConnStr(targetSingleThreadedPort)
  lazy val targetAkkaStreamsConnString: String = makeConnStr(targetAkkaStreamsPort)

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    createOriginDb()

    val originConnString = makeConnStr(originPort)
    val sharedArgs = Array("--originDbConnStr", originConnString, "--originDbParallelism", "10", "--targetDbParallelism", "10")
    val stArgs = programArgs ++ sharedArgs ++ Array("--targetDbConnStr", targetSingleThreadedConnString)
    val asArgs = programArgs ++ sharedArgs ++ Array("--targetDbConnStr", targetAkkaStreamsConnString)
    val singleThreadedConfig = CommandLineParser.parser.parse(stArgs, Config()).get
    val akkaStreamsConfig = CommandLineParser.parser.parse(asArgs, Config()).get

    originDb = profile.backend.Database.forURL(singleThreadedConfig.originDbConnectionString)
    setupDDL()
    setupDML()
    setupTargetDbs()
    targetDbSt = profile.backend.Database.forURL(singleThreadedConfig.targetDbConnectionString)
    targetDbAs = profile.backend.Database.forURL(akkaStreamsConfig.targetDbConnectionString)

    // `schemaInfo` and `baseQueries` will be the same regardless of whether we use `singleThreadedConfig` or `akkaStreamsConfig`
    val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(singleThreadedConfig)
    val baseQueries = BaseQueries.get(singleThreadedConfig, schemaInfo)

    ApplicationSingleThreaded.run(singleThreadedConfig, schemaInfo, baseQueries)
    val futureResult = ApplicationAkkaStreams.run(akkaStreamsConfig, schemaInfo, baseQueries)
    Await.result(futureResult, Duration.Inf)

    postSubset()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    originDb.close()
    targetDbSt.close()
    targetDbAs.close()
  }

  protected def assertCount[T <: AbstractTable[_]](tq: TableQuery[T], expected: Long): Unit = {
    import profile.api._
    assert(Await.result(targetDbSt.run(tq.size.result), Duration.Inf) === expected)
    assert(Await.result(targetDbAs.run(tq.size.result), Duration.Inf) === expected)
  }

  // Helper to get around intelliJ warnings, technically it could compile just with the Long version
  protected def assertThat(action: DBIOAction[Option[Int], profile.api.NoStream, Effect.Read], expected: Long): Unit = {
    assert(Await.result(targetDbSt.run(action), Duration.Inf) === Some(expected))
    assert(Await.result(targetDbAs.run(action), Duration.Inf) === Some(expected))
  }

  protected def assertThatLong(action: DBIOAction[Option[Long], profile.api.NoStream, Effect.Read], expected: Long): Unit = {
    assert(Await.result(targetDbSt.run(action), Duration.Inf) === Some(expected))
    assert(Await.result(targetDbAs.run(action), Duration.Inf) === Some(expected))
  }
}

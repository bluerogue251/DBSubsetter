package e2e

import e2e.missingfk.{Inserts, Tables}
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
  val profile: slick.jdbc.JdbcProfile

  def originPort: Int

  def makeConnStr(port: Int): String

  def programArgs: Array[String]

  def createOriginDb(): Unit

  def setupTargetDbs(): Unit

  def postSubset(): Unit

  var originDb: JdbcBackend#DatabaseDef = _
  var targetDbSt: JdbcBackend#DatabaseDef = _
  var targetDbAs: JdbcBackend#DatabaseDef = _
  var singleThreadedConfig: Config = _
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
    singleThreadedConfig = CommandLineParser.parser.parse(stArgs, Config()).get
    val akkaStreamsConfig = CommandLineParser.parser.parse(asArgs, Config()).get

    originDb = profile.backend.Database.forURL(singleThreadedConfig.originDbConnectionString)
    createOriginDbDdl()
    insertOriginDbData()
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

  protected def assertThat(action: DBIOAction[Option[Int], profile.api.NoStream, Effect.Read], expected: Long): Unit = {
    assert(Await.result(targetDbSt.run(action), Duration.Inf) === expected)
    assert(Await.result(targetDbAs.run(action), Duration.Inf) === expected)
  }

  def createOriginDbDdl(): Unit = {
    import profile.api._
    val tables = new Tables(profile)
    val fut = originDb.run(DBIO.seq(tables.schema.create))
    Await.result(fut, Duration.Inf)
  }

  def insertOriginDbData(): Unit = {
    val db = profile.backend.Database.forURL(singleThreadedConfig.originDbConnectionString)
    val fut = db.run(
      new Inserts(profile).dbioSeq
    )
    Await.result(fut, Duration.Inf)
  }
}

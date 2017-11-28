package e2e

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.basic.BasicBackend
import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.{ColumnName, SchemaInfoRetrieval, SchemaName, TableName}
import trw.dbsubsetter.workflow.BaseQueries
import trw.dbsubsetter.{ApplicationAkkaStreams, ApplicationSingleThreaded}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class AbstractEndToEndTest extends FunSuite with BeforeAndAfterAll {
  def originPort: Int

  def makeConnStr(port: Int): String

  def programArgs: Array[String]

  lazy val originConnString: String = makeConnStr(originPort)

  def createOriginDb(): Unit

  def createSlickOriginDbConnection(): BasicBackend#DatabaseDef

  def createOriginDbDdl(): Unit

  def insertOriginDbData(): Unit

  var originDb: BasicBackend#DatabaseDef = _

  def setupTargetDbs(): Unit

  def postSubset(): Unit

  var singleThreadedConfig: Config = _
  var targetSingleThreadedConn: Connection = _
  var targetAkkaStreamsConn: Connection = _

  lazy val targetSingleThreadedPort: Int = originPort + 1
  lazy val targetAkkaStreamsPort: Int = originPort + 2

  lazy val targetSingleThreadedConnString: String = makeConnStr(targetSingleThreadedPort)

  lazy val targetAkkaStreamsConnString: String = makeConnStr(targetAkkaStreamsPort)

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    createOriginDb()

    val sharedArgs = Array("--originDbConnStr", originConnString, "--originDbParallelism", "10", "--targetDbParallelism", "10")
    val stArgs = programArgs ++ sharedArgs ++ Array("--targetDbConnStr", targetSingleThreadedConnString)
    val asArgs = programArgs ++ sharedArgs ++ Array("--targetDbConnStr", targetAkkaStreamsConnString)
    singleThreadedConfig = CommandLineParser.parser.parse(stArgs, Config()).get
    val akkaStreamsConfig = CommandLineParser.parser.parse(asArgs, Config()).get

    originDb = createSlickOriginDbConnection()
    createOriginDbDdl()
    insertOriginDbData()
    setupTargetDbs()

    // `schemaInfo` and `baseQueries` will be the same regardless of whether we use `singleThreadedConfig` or `akkaStreamsConfig`
    val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(singleThreadedConfig)
    val baseQueries = BaseQueries.get(singleThreadedConfig, schemaInfo)

    ApplicationSingleThreaded.run(singleThreadedConfig, schemaInfo, baseQueries)
    val futureResult = ApplicationAkkaStreams.run(akkaStreamsConfig, schemaInfo, baseQueries)
    Await.result(futureResult, Duration.Inf)

    postSubset()

    targetSingleThreadedConn = DriverManager.getConnection(targetSingleThreadedConnString)
    targetSingleThreadedConn.setReadOnly(true)
    targetAkkaStreamsConn = DriverManager.getConnection(targetSingleThreadedConnString)
    targetAkkaStreamsConn.setReadOnly(true)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    originDb.close()
    targetSingleThreadedConn.close()
    targetAkkaStreamsConn.close()
  }

  protected def assertCount(schema: SchemaName, table: TableName, whereClauseOpt: Option[String], expected: Long): Unit = {
    val baseSql = s"""select count(*) from "$schema"."$table""""
    val sql = whereClauseOpt.fold(baseSql) { wc => s"$baseSql where $wc" }

    val singleThreadedJdbcResult = targetSingleThreadedConn.createStatement().executeQuery(sql)
    singleThreadedJdbcResult.next()
    val singleThreadedCount = singleThreadedJdbcResult.getLong(1)
    assert(singleThreadedCount === expected)

    val akkaStreamsJdbcResult = targetAkkaStreamsConn.createStatement().executeQuery(sql)
    akkaStreamsJdbcResult.next()
    val akkaStreamsCount = akkaStreamsJdbcResult.getLong(1)
    assert(akkaStreamsCount === expected)
  }

  protected def assertSum(schema: SchemaName, table: TableName, column: ColumnName, expected: Long): Unit = {
    val singleThreadedJdbcResult = targetSingleThreadedConn.createStatement().executeQuery(s"""select sum("$column") from "$schema"."$table"""")
    singleThreadedJdbcResult.next()
    val singleThreadedSum = singleThreadedJdbcResult.getLong(1)
    assert(singleThreadedSum === expected)

    val akkaStreamsJdbcResult = targetAkkaStreamsConn.createStatement().executeQuery(s"""select sum("$column") from "$schema"."$table"""")
    akkaStreamsJdbcResult.next()
    val akkaStreamsSum = akkaStreamsJdbcResult.getLong(1)
    assert(akkaStreamsSum === expected)
  }
}

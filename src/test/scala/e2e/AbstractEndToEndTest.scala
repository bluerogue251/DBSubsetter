package e2e

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.{ColumnName, SchemaInfoRetrieval, SchemaName, TableName}
import trw.dbsubsetter.workflow.BaseQueries
import trw.dbsubsetter.{ApplicationAkkaStreams, ApplicationSingleThreaded}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.sys.process._


abstract class AbstractEndToEndTest extends FunSuite with BeforeAndAfterAll {
  def dataSetName: String
  def originPort: Int
  def programArgs: Array[String]

  var targetSingleThreadedConn: Connection = _
  var targetAkkaStreamsConn: Connection = _

  def targetSingleThreadedPort: Int = originPort + 1

  def targetAkkaStreamsPort: Int = originPort + 2
  def originDbName = s"${dataSetName}_origin"

  def targetDbSingleThreadedName = s"${dataSetName}_target_st"

  def targetDbAkkaStreamsName = s"${dataSetName}_target_as"

  def originConnString = s"jdbc:mysql://localhost:$originPort/$originDbName?user=root"

  def targetSingleThreadedConnString = s"jdbc:mysql://0.0.0.0:$targetSingleThreadedPort/$targetDbSingleThreadedName?user=root"

  def targetAkkaStreamsConnString = s"jdbc:mysql://0.0.0.0:$targetAkkaStreamsPort/$targetDbAkkaStreamsName?user=root"

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    //    s"./util/reset_origin_db.sh $dataSetName $originDbName $originPort".!!
    //    s"./util/reset_target_db.sh $originDbName $originPort $targetDbSingleThreadedName $targetSingleThreadedPort".!!
    //    s"./util/reset_target_db.sh $originDbName $originPort $targetDbAkkaStreamsName $targetAkkaStreamsPort".!!

    val parallelismArgs = Array(
      "--originDbParallelism", "10",
      "--targetDbParallelism", "10"
    )
    val singleThreadedArgs = programArgs ++ parallelismArgs ++ Array(
      "--originDbConnStr", originConnString,
      "--targetDbConnStr", targetSingleThreadedConnString,
      "--singleThreadedDebugMode"
    )
    val akkaStreamsArgs = programArgs ++ parallelismArgs ++ Array(
      "--originDbConnStr", originConnString,
      "--targetDbConnStr", targetAkkaStreamsConnString,
    )
    val singleThreadedConfig = CommandLineParser.parser.parse(singleThreadedArgs, Config()).get
    val akkaStreamsConfig = CommandLineParser.parser.parse(akkaStreamsArgs, Config()).get
    // `schemaInfo` and `baseQueries` will be the same regardless of whether we use `singleThreadedConfig` or `akkaStreamsConfig`
    val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(singleThreadedConfig)
    val baseQueries = BaseQueries.get(singleThreadedConfig, schemaInfo)

    ApplicationSingleThreaded.run(singleThreadedConfig, schemaInfo, baseQueries)
    val futureResult = ApplicationAkkaStreams.run(akkaStreamsConfig, schemaInfo, baseQueries)
    Await.result(futureResult, Duration.Inf)

    s"./util/post_subset_target.sh $originDbName $originPort $targetDbSingleThreadedName $targetSingleThreadedPort".!!
    s"./util/post_subset_target.sh $originDbName $originPort $targetDbAkkaStreamsName $targetAkkaStreamsPort".!!

    targetSingleThreadedConn = DriverManager.getConnection(targetSingleThreadedConnString)
    targetSingleThreadedConn.setReadOnly(true)

    targetAkkaStreamsConn = DriverManager.getConnection(targetSingleThreadedConnString)
    targetAkkaStreamsConn.setReadOnly(true)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
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

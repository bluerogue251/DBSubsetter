package e2e

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.{SchemaInfo, SchemaInfoRetrieval}
import trw.dbsubsetter.workflow.BaseQueries
import trw.dbsubsetter.{ApplicationAkkaStreams, ApplicationSingleThreaded}
import util.db.DatabaseContainerSet

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class AbstractEndToEndTest extends FunSuite with BeforeAndAfterAll {

  protected val profile: slick.jdbc.JdbcProfile

  protected def createContainers(): DatabaseContainerSet

  protected var containers: DatabaseContainerSet

  protected def prepareOriginDb(): Unit

  protected def prepareTargetDbs(): Unit

  protected def programArgs: Array[String]

  protected def postSubset(): Unit

  var originSlick: profile.backend.DatabaseDef
  var targetSingleThreadedSlick: profile.backend.DatabaseDef
  var targetAkkaStreamsSlick: profile.backend.DatabaseDef

  var singleThreadedRuntimeMillis: Long = 0
  var akkStreamsRuntimeMillis: Long = 0
  var schemaInfo: SchemaInfo = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    /*
     * Spin up docker containers for the origin and target DBs
     */
    containers = createContainers()

    /*
     * Create slick connections to the origin and target DBs. These connections are utilities for testing
     * purposes such as populating the origin DB with DDL/DML, querying the target DBs after subsetting
     * to make assertions about their contents, etc.
     */
    originSlick = profile.backend.Database.forURL(containers.origin.db.connectionString)
    targetSingleThreadedSlick = profile.backend.Database.forURL(containers.targetSingleThreaded.db.connectionString)
    targetAkkaStreamsSlick = profile.backend.Database.forURL(containers.targetAkkaStreams.db.connectionString)

    /*
     * Set up the DDL and DML in the origin DB
     */
    prepareOriginDb()

    /*
     * Set up the DDL (but NOT the DML) in the target DB
     */
    prepareTargetDbs()


    /*
     * Run subsetting to copy a subset of the DML from the origin DB to the target DBs
     */
    runSubsetInSingleThreadedMode()
    runSubsetInAkkaStreamsMode()

    /*
     * Do any steps necessary after subsetting, such as re-enabling foreign keys, re-adding indices
     * to the target DBs, etc.
     */
    postSubset()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    originSlick.close()
    targetSingleThreadedSlick.close()
    targetAkkaStreamsSlick.close()
  }

  private def runSubsetInSingleThreadedMode(): Unit = {
    val args: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--targetDbConnStr", containers.targetSingleThreaded.db.connectionString
    )

    val config: Config = CommandLineParser.parser.parse(args, Config()).get
    schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
    val baseQueries = BaseQueries.get(config, schemaInfo)

    val startSingleThreaded = System.nanoTime()
    ApplicationSingleThreaded.run(config, schemaInfo, baseQueries)
    singleThreadedRuntimeMillis = (System.nanoTime() - startSingleThreaded) / 1000000
    println(s"Single Threaded Took $singleThreadedRuntimeMillis milliseconds")
  }

  private def runSubsetInAkkaStreamsMode(): Unit = {
    val args: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--originDbParallelism", "10",
      "--targetDbParallelism", "10",
      "--targetDbConnStr", containers.targetAkkaStreams.db.connectionString
    )

    val config: Config = CommandLineParser.parser.parse(args, Config()).get
    schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
    val baseQueries = BaseQueries.get(config, schemaInfo)

    val startAkkaStreams = System.nanoTime()
    val futureResult = ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
    Await.result(futureResult, Duration.Inf)
    akkStreamsRuntimeMillis = (System.nanoTime() - startAkkaStreams) / 1000000
    println(s"Akka Streams Took $akkStreamsRuntimeMillis milliseconds")
  }
}

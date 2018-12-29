package e2e

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.jdbc.JdbcBackend
import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.{SchemaInfo, SchemaInfoRetrieval}
import trw.dbsubsetter.workflow.BaseQueries
import trw.dbsubsetter.{ApplicationAkkaStreams, ApplicationSingleThreaded}
import util.db.{Database, DatabaseContainerSet}
import util.docker.ContainerUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class AbstractEndToEndTest[T <: Database] extends FunSuite with BeforeAndAfterAll {
  /*
   * Concrete test classes must override the following
   */
  protected def profile: slick.jdbc.JdbcProfile

  protected def startContainers(): DatabaseContainerSet[T]

  protected def createEmptyDatabases(): Unit

  protected def prepareOriginDDL(): Unit

  protected def prepareOriginDML(): Unit

  protected def prepareTargetDDL(): Unit

  protected def programArgs: Array[String]

  protected def postSubset(): Unit

  /*
   * Docker containers holding the origin and target DBs (do not override)
   */
  protected var containers: DatabaseContainerSet[T] = _

  /*
   * Slick testing utility connections (do not override)
   */
  protected var originSlick: JdbcBackend#DatabaseDef = _

  protected var targetSingleThreadedSlick: JdbcBackend#DatabaseDef = _

  protected var targetAkkaStreamsSlick: JdbcBackend#DatabaseDef = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    /*
     * Spin up docker containers for the origin and target DBs
     */
    containers = startContainers()

    /*
     * Create empty origin and target databases with correct database names
     */
    createEmptyDatabases()

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
    prepareOriginDDL()
    prepareOriginDML()

    /*
     * Set up the DDL (but NOT the DML) in the target DB
     */
    prepareTargetDDL()


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

    /*
     * All of our setup is now done. We are now ready to make assertions on the contents of the
     * target DBs to ensure that our program copied the correct data from the origin to the target
     * DBs.
     */
  }

  override protected def afterAll(): Unit = {
    super.afterAll()

    /*
     * Close slick JDBC connections
     */
    originSlick.close()
    targetSingleThreadedSlick.close()
    targetAkkaStreamsSlick.close()

    /*
     * Remove all containers
     */
    ContainerUtil.rm(containers.origin.name)
    ContainerUtil.rm(containers.targetSingleThreaded.name)
    ContainerUtil.rm(containers.targetAkkaStreams.name)
  }

  private def runSubsetInSingleThreadedMode(): Unit = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--targetDbConnStr", containers.targetSingleThreaded.db.connectionString
    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    val config: Config = CommandLineParser.parser.parse(finalArgs, Config()).get
    val schemaInfo: SchemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
    val baseQueries = BaseQueries.get(config, schemaInfo)

    val startSingleThreaded = System.nanoTime()
    ApplicationSingleThreaded.run(config, schemaInfo, baseQueries)
    val singleThreadedRuntimeMillis = (System.nanoTime() - startSingleThreaded) / 1000000
    println(s"Single Threaded Took $singleThreadedRuntimeMillis milliseconds")
  }

  private def runSubsetInAkkaStreamsMode(): Unit = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--originDbParallelism", "10",
      "--targetDbParallelism", "10",
      "--targetDbConnStr", containers.targetAkkaStreams.db.connectionString
    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    val config: Config = CommandLineParser.parser.parse(finalArgs, Config()).get
    val schemaInfo: SchemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
    val baseQueries = BaseQueries.get(config, schemaInfo)

    val startAkkaStreams = System.nanoTime()
    val futureResult = ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
    Await.result(futureResult, Duration.Inf)
    val akkStreamsRuntimeMillis = (System.nanoTime() - startAkkaStreams) / 1000000
    println(s"Akka Streams Took $akkStreamsRuntimeMillis milliseconds")
  }
}

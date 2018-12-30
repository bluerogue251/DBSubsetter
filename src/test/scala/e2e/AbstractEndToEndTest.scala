package e2e

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.jdbc.JdbcBackend
import util.db.{Database, DatabaseContainerSet}
import util.docker.ContainerUtil
import util.runner.TestSubsetRunner

abstract class AbstractEndToEndTest[T <: Database] extends FunSuite with BeforeAndAfterAll {
  /*
   * Concrete test classes must override the following
   */
  protected def profile: slick.jdbc.JdbcProfile

  protected def startOriginDatabase(): Unit

  protected def startTargetDatabases(): Unit

  protected def containers: DatabaseContainerSet[T]

  protected def prepareOriginDDL(): Unit

  protected def prepareOriginDML(): Unit

  protected def prepareTargetDDL(): Unit

  protected def programArgs: Array[String]

  protected def runSubsetInSingleThreadedMode(): Unit = {
    TestSubsetRunner.runSubsetInSingleThreadedMode(containers, programArgs)
  }

  protected def runSubsetInAkkaStreamsMode(): Unit = {
    TestSubsetRunner.runSubsetInAkkaStreamsMode(containers, programArgs)
  }

  protected def postSubset(): Unit

  protected def teardownOriginDatabase(): Unit = {
    ContainerUtil.rm(containers.origin.name)
  }

  protected def teardownTargetDatabases(): Unit = {
    ContainerUtil.rm(containers.targetSingleThreaded.name)
    ContainerUtil.rm(containers.targetAkkaStreams.name)
  }

  /*
   * Slick testing utility connections (do not override)
   */
  protected var originSlick: JdbcBackend#DatabaseDef = _

  protected var targetSingleThreadedSlick: JdbcBackend#DatabaseDef = _

  protected var targetAkkaStreamsSlick: JdbcBackend#DatabaseDef = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    /*
     * Spin up containerized databases for the origin and targets
     */
    startOriginDatabase()
    startTargetDatabases()

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
     * Remove any containers which need to be cleaned up
     */
    teardownOriginDatabase()
    teardownTargetDatabases()
  }
}

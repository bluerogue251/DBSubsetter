package e2e

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.jdbc.JdbcBackend
import util.db.{Database, DatabaseSet}
import util.runner.TestSubsetRunner

abstract class AbstractEndToEndTest[T <: Database] extends FunSuite with BeforeAndAfterAll {
  /*
   * Concrete test classes must override the following
   */
  protected def profile: slick.jdbc.JdbcProfile

  protected def createOriginDatabase(): Unit

  protected def createTargetDatabases(): Unit

  protected def dbs: DatabaseSet[T]

  protected def prepareOriginDDL(): Unit

  protected def prepareOriginDML(): Unit

  protected def prepareTargetDDL(): Unit

  protected def programArgs: Array[String]

  protected def runSubsetInSingleThreadedMode(): Unit = {
    TestSubsetRunner.runSubsetInSingleThreadedMode(dbs, programArgs)
  }

  protected def runSubsetInAkkaStreamsMode(): Unit = {
    TestSubsetRunner.runSubsetInAkkaStreamsMode(dbs, programArgs)
  }

  protected def postSubset(): Unit

  protected def teardownOriginContainer(): Unit = {} // No-op by default

  protected def teardownTargetContainers(): Unit = {} // No-op by default

  /*
   * Slick testing utility connections (do not override)
   */
  protected var originSlick: JdbcBackend#DatabaseDef = _

  protected var targetSingleThreadedSlick: JdbcBackend#DatabaseDef = _

  protected var targetAkkaStreamsSlick: JdbcBackend#DatabaseDef = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    /*
     * Create origin and target databases
     */
    createOriginDatabase()
    createTargetDatabases()

    /*
     * Create slick connections to the origin and target DBs. These connections are utilities for testing
     * purposes such as populating the origin DB with DDL/DML, querying the target DBs after subsetting
     * to make assertions about their contents, etc.
     */
    originSlick = profile.backend.Database.forURL(dbs.origin.connectionString)
    targetSingleThreadedSlick = profile.backend.Database.forURL(dbs.targetSingleThreaded.connectionString)
    targetAkkaStreamsSlick = profile.backend.Database.forURL(dbs.targetAkkaStreams.connectionString)

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
     * Remove any containers as necessary
     */
    teardownOriginContainer()
    teardownTargetContainers()
  }
}

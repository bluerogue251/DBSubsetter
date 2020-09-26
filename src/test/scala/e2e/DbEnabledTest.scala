package e2e

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import slick.jdbc.JdbcBackend
import util.db.{Database, DatabaseSet}

/**
  * A test which requires access to a running database.
  */
abstract class DbEnabledTest[T <: Database] extends FunSuite with BeforeAndAfterAll {
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

package load

import e2e.{AbstractEndToEndTest, PostgresqlEndToEndTestUtil}
import slick.dbio.DBIO
import util.Ports
import util.db._
import util.docker.ContainerUtil
import util.runner.TestSubsetRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/*
 * Extends AbstractEndToEndTest and adds two new features:
 *   1. Ability to track how long a subsetting run took and assert that it was within expected limits
 *   2. Ability to quickly load data into the origin DB and to not destroy and rebuild the origin DB each test run
 */
trait LoadTest[T <: Database] { this: AbstractEndToEndTest[T] =>

  protected def singleThreadedRuntimeLimitMillis: Long

  protected def akkaStreamsRuntimeLimitMillis: Long

  private var singleThreadedRuntimeMillis: Long = _

  private var akkaStreamsRuntimeMillis: Long = _

  private lazy val mustReCreateOriginDb: Boolean = !ContainerUtil.exists(containers.origin.name)

  /*
   * Only to be used when manually changing the origin db definition. In this case, the origin DB needs
   * to be completely rebuilt from scratch using Slick definitions, as opposed to being efficiently
   * loaded from an existing dump file. Only set this to `true` if you really know what you are doing, and
   * if you are prepared to make sure the dump file stored in S3 gets updated to your latest version.
   */
  private val usePainfullySlowDbSetupOverride: Boolean = false

  protected def loadTestRecreateOriginContainer(): Unit

  override protected def startOriginContainer(): Unit = {
    if (mustReCreateOriginDb) {
      loadTestRecreateOriginContainer()
    } else {
      ContainerUtil.start(containers.origin.name)
    }
  }

  override protected def createOriginDatabase(): Unit = {
    if (mustReCreateOriginDb) {
      PostgresqlEndToEndTestUtil.createDb(containers.origin.name, containers.origin.db.name)
    }
  }

  override protected def containers: DatabaseContainerSet[PostgreSQLDatabase] = {
    val defaults = super.containers

    val originDb = new PostgreSQLDatabase("school_db", Ports.postgresSchoolDbOrigin)
    val originContainer = new PostgreSQLContainer("school_db_origin_postgres", originDb)

    new DatabaseContainerSet[PostgreSQLDatabase](
      originContainer,
      defaults.targetSingleThreaded,
      defaults.targetAkkaStreams
    )
  }

  override protected def prepareOriginDDL(): Unit = {
    (mustReCreateOriginDb, usePainfullySlowDbSetupOverride) match {
      case (false, _) => // No action necessary
      case (true, false) => // Load origin DB from dump file stored in S3
        val dumpUrl = "https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz"
        s"./src/test/util/load_postgres_db_from_s3.sh $dumpUrl ${containers.origin.name} ${containers.origin.db.name}".!!
      case (true, true) => // Recreate origin DB from original slick definitions
        val createSchemaStatements: DBIO[Unit] = DBIO.seq(
          sqlu"create schema school_db",
          sqlu"""create schema "Audit""""
        )
        Await.ready(originSlick.run(createSchemaStatements), Duration.Inf)
        super.prepareOriginDDL()
    }
  }

  override protected def prepareOriginDML(): Unit = {
    (mustReCreateOriginDb, usePainfullySlowDbSetupOverride) match {
      case (false, _) => // No action necessary
      case (true, false) => // No action necessary (already done in prepareOriginDDL)
      case (true, true) => super.prepareOriginDML() // We have to populate it from scratch
    }
  }

  override protected def runSubsetInSingleThreadedMode(): Unit = {
    singleThreadedRuntimeMillis = TestSubsetRunner.runSubsetInSingleThreadedMode(containers, programArgs)
  }

  override protected def runSubsetInAkkaStreamsMode(): Unit = {
    akkaStreamsRuntimeMillis = TestSubsetRunner.runSubsetInAkkaStreamsMode(containers, programArgs)
  }

  test("Single threaded runtime did not significantly increase") {
    assert(singleThreadedRuntimeMillis < singleThreadedRuntimeLimitMillis)
  }

  test("Akka Streams runtime did not significantly increase") {
    assert(akkaStreamsRuntimeMillis < akkaStreamsRuntimeLimitMillis)
  }
}

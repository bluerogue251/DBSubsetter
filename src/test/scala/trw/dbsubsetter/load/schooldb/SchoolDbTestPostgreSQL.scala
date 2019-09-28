package trw.dbsubsetter.load.schooldb

import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import trw.dbsubsetter.e2e.{AbstractPostgresqlEndToEndTest, PostgresqlEndToEndTestUtil, SharedTestContainers}
import trw.dbsubsetter.load.LoadTest
import trw.dbsubsetter.util.Ports
import trw.dbsubsetter.util.db.{DatabaseContainer, DatabaseContainerSet, PostgreSQLContainer, PostgreSQLDatabase}
import trw.dbsubsetter.util.docker.ContainerUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.sys.process._

class SchoolDbTestPostgreSQL extends AbstractPostgresqlEndToEndTest with LoadTest[PostgreSQLDatabase] with SchoolDbTest {

  override val singleThreadedRuntimeLimitMillis: Long = 210000 // 210 seconds

  override val akkaStreamsRuntimeLimitMillis: Long = 25000 // 25 seconds

  private lazy val mustReCreateOriginDb: Boolean = !ContainerUtil.exists(containers.origin.name)

  /*
    * Only to be used when manually changing the origin db definition. In this case, the origin DB needs
    * to be completely rebuilt from scratch using Slick definitions, as opposed to being efficiently
    * loaded from an existing dump file. Only set this to `true` if you really know what you are doing, and
    * if you are prepared to make sure the dump file stored in S3 gets updated to your latest version.
    */
  private val skipOriginDbPerformanceOptimization: Boolean = false

  override protected def startOriginContainer(): Unit = {
    if (mustReCreateOriginDb) {
      DatabaseContainer.recreatePostgreSQL(containers.origin.name, containers.origin.db.port)
    } else {
      ContainerUtil.start(containers.origin.name)
    }
  }

  override protected def startTargetContainers(): Unit = SharedTestContainers.postgres

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
    (mustReCreateOriginDb, skipOriginDbPerformanceOptimization) match {
      case (false, _) => // No action necessary
      case (true, false) => // Load origin DB from dump file stored in S3
        val dumpUrl = "https://s3.amazonaws.com/db-subsetter/trw.dbsubsetter.load-test/school-db/pgdump.sql.gz"
        s"./src/test/trw.dbsubsetter.util/load_postgres_db_from_s3.sh $dumpUrl ${containers.origin.name} ${containers.origin.db.name}".!!
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
    (mustReCreateOriginDb, skipOriginDbPerformanceOptimization) match {
      case (false, _) => // No action necessary
      case (true, false) => // No action necessary (already done in prepareOriginDDL)
      case (true, true) => super.prepareOriginDML() // We have to populate it from scratch
    }
  }

  override protected val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )
}

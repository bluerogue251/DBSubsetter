package load.schooldb

import e2e.AbstractPostgresqlEndToEndTest
import load.LoadTest
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import util.db.PostgreSQLDatabase

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.sys.process._

class SchoolDbTestPostgreSQL extends AbstractPostgresqlEndToEndTest with LoadTest[PostgreSQLDatabase] with SchoolDbTest {

  /*
   * These values are configured for the Drone CI environment and are supposed to be pretty lenient,
   * only catching major performance degradations. For more rigorous testing, do load testing on AWS.
   * See the load-test directory for details.
   */
  override val singleThreadedRuntimeLimitMillis: Long = 120000 // 120 seconds

  override val akkaStreamsRuntimeLimitMillis: Long = 25000 // 25 seconds

  /*
    * Only to be used when manually changing the origin db definition. In this case, the origin DB needs
    * to be completely rebuilt from scratch using Slick definitions, as opposed to being efficiently
    * loaded from an existing dump file. Only set this to `true` if you really know what you are doing, and
    * if you are prepared to make sure the dump file stored in S3 gets updated to your latest version.
    */
  private val updateOriginDb: Boolean = false

  override protected def prepareOriginDDL(): Unit = {
    (updateOriginDb) match {
      case (false) => // Load origin DB from dump file stored in S3
        val dumpUrl = "https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz"
        s"./src/test/util/load_postgres_db_from_s3.sh $dumpUrl ${dbs.origin.host} ${dbs.origin.name}".!!
      case (true) => // Recreate origin DB from original slick definitions
        val createSchemaStatements: DBIO[Unit] = DBIO.seq(
          sqlu"create schema school_db",
          sqlu"""create schema "Audit""""
        )
        Await.ready(originSlick.run(createSchemaStatements), Duration.Inf)
        super.prepareOriginDDL()
    }
  }

  override protected def prepareOriginDML(): Unit = {
    (updateOriginDb) match {
      case (false) => // No action necessary (already done in prepareOriginDDL)
      case (true) => super.prepareOriginDML() // We have to populate it from scratch
    }
  }

  override protected val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2"
  )
}

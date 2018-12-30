package load.schooldb

import e2e.AbstractPostgresqlEndToEndTest
import load.LoadTest
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import util.db.PostgreSQLDatabase

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SchoolDbTestPostgreSQL extends AbstractPostgresqlEndToEndTest with LoadTest[PostgreSQLDatabase] with SchoolDbTest {

  override val singleThreadedRuntimeLimitMillis: Long = 220000

  override val akkaStreamsRuntimeLimitMillis: Long = 25000

  override protected val originPort = 5453

  override protected val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )

  override protected def prepareOriginDDL(): Unit = {
    val createSchemaStatements: DBIO[Unit] = DBIO.seq(
      sqlu"create schema school_db",
      sqlu"""create schema "Audit""""
    )
    Await.ready(originSlick.run(createSchemaStatements), Duration.Inf)
    super.prepareOriginDML()
  }
}

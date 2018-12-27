package load.schooldb

import e2e.AbstractPostgresqlEndToEndTest
import load.LoadTest
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SchoolDbPostgresqlTest extends AbstractPostgresqlEndToEndTest with SchoolDbTestCases with LoadTest {
  override val originPort = 5453
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )

  override def setupOriginDDL(): Unit = {
    val createSchemaStatements: DBIO[Unit] = DBIO.seq(
      sqlu"create schema school_db",
      sqlu"""create schema "Audit""""
    )
    Await.ready(originDb.run(createSchemaStatements), Duration.Inf)
    super.setupOriginDDL()
  }

  override val singleThreadedRuntimeThreshold: Long = 220000

  override val akkaStreamsRuntimeThreshold: Long = 25000
}

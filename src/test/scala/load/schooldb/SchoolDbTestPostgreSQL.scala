package load.schooldb

import e2e.AbstractPostgresqlEndToEndTest
import load.LoadTest
import util.db.PostgreSQLDatabase

class SchoolDbTestPostgreSQL extends AbstractPostgresqlEndToEndTest with LoadTest[PostgreSQLDatabase] with SchoolDbTest {

  override val singleThreadedRuntimeLimitMillis: Long = 220000

  override val akkaStreamsRuntimeLimitMillis: Long = 25000

  override protected val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )
}

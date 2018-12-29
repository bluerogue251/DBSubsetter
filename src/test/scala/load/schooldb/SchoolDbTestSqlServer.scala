package load.schooldb

import e2e.AbstractSqlServerEndToEndTest

import scala.sys.process._

class SchoolDbTestSqlServer extends AbstractSqlServerEndToEndTest with SchoolDbTest {
  override protected val port = 5456

  override protected val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )

  override protected def prepareOriginDDL(): Unit = {
    s"./src/test/util/create_schema_sqlserver.sh ${containers.origin.name} $testName school_db".!!
    s"./src/test/util/create_schema_sqlserver.sh ${containers.origin.name} $testName Audit".!!
    super.prepareOriginDDL()
  }

// TODO: put back when we reintroduce load tests
//  override val singleThreadedRuntimeThreshold: Long = 110000
//
//  override val akkaStreamsRuntimeThreshold: Long = 25000
}

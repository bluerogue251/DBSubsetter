package load.schooldb

import e2e.AbstractSqlServerEndToEndTest
import load.LoadTest

import scala.sys.process._

class SchoolDbSqlServerTest extends AbstractSqlServerEndToEndTest with SchoolDbTestCases with LoadTest {
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
    s"./src/test/util/create_schema_sqlserver.sh ${containers.origin.name} $dataSetName school_db".!!
    s"./src/test/util/create_schema_sqlserver.sh ${containers.origin.name} $dataSetName Audit".!!
    super.prepareOriginDDL()
  }

  override val singleThreadedRuntimeThreshold: Long = 110000

  override val akkaStreamsRuntimeThreshold: Long = 25000
}

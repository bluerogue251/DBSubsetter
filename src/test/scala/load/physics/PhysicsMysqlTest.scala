package load.physics

import e2e.AbstractMysqlEndToEndTest
import load.LoadTest

class PhysicsMysqlTest extends AbstractMysqlEndToEndTest with PhysicsTestCases with LoadTest {
  override val originPort = 5570
  override val programArgs = Array(
    "--schemas", "physics",
    "--baseQuery", "physics.scientists ::: id % 2 = 0 ::: includeChildren",
    "--baseQuery", "physics.experiment_plans ::: id % 19 = 0 ::: includeChildren"
  )

  //  override def createOriginDb(): Unit = s"docker start school_db_origin_mysql".!

  //  override def setupDDL(): Unit = {}

  //  override def setupDML(): Unit = {}

  //  override def setupTargetDbs(): Unit = {}

  override val singleThreadedRuntimeThreshold: Long = 2

  override val akkaStreamsRuntimeThreshold: Long = 2
}

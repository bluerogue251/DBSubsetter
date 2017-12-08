package load.physics

import e2e.AbstractSqlServerEndToEndTest
import load.LoadTest

class PhysicsSqlServerTest extends AbstractSqlServerEndToEndTest with PhysicsTestCases with LoadTest {
  override val originPort = 5576
  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.scientists ::: id % 2 = 0 ::: includeChildren",
    "--baseQuery", "dbo.experiment_plans ::: id % 19 = 0 ::: includeChildren",
    "--baseQuery", "dbo.particle_domain ::: true ::: excludeChildren",
    "--baseQuery", "dbo.quantum_domain ::: true ::: excludeChildren",
    "--baseQuery", "dbo.gravitational_wave_domain ::: true ::: excludeChildren"
  )

  //  override def createOriginDb(): Unit = s"docker start school_db_origin_mysql".!

  //  override def setupDDL(): Unit = {}

  //  override def setupDML(): Unit = {}

  //  override def setupTargetDbs(): Unit = {}

  override val singleThreadedRuntimeThreshold: Long = 2

  override val akkaStreamsRuntimeThreshold: Long = 2
}

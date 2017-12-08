package load.physics

import e2e.AbstractPostgresqlEndToEndTest
import load.LoadTest

class PhysicsPostgresqlTest extends AbstractPostgresqlEndToEndTest with PhysicsTestCases with LoadTest {
  override val originPort = 5573
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.scientists ::: id % 2 = 0 ::: includeChildren",
    "--baseQuery", "public.experiment_plans ::: id % 19 = 0 ::: includeChildren",
    "--baseQuery", "public.particle_domain ::: true ::: excludeChildren",
    "--baseQuery", "public.quantum_domain ::: true ::: excludeChildren",
    "--baseQuery", "public.gravitational_wave_domain ::: true ::: excludeChildren"
  )

  //  override def createOriginDb(): Unit = s"docker start school_db_origin_mysql".!

  //  override def setupDDL(): Unit = {}

  //  override def setupDML(): Unit = {}

  //  override def setupTargetDbs(): Unit = {}

  override val singleThreadedRuntimeThreshold: Long = 2

  override val akkaStreamsRuntimeThreshold: Long = 2
}

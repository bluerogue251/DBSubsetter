package load.physics

import e2e.AbstractPostgresqlEndToEndTest
import load.LoadTest

import scala.sys.process._

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

  override def createOriginDb(): Unit = s"docker start physics_origin_postgres".!

  override def setupDDL(): Unit = {}

  override def setupDML(): Unit = {}

  override val singleThreadedRuntimeThreshold: Long = 400000

  override val akkaStreamsRuntimeThreshold: Long = 2
}

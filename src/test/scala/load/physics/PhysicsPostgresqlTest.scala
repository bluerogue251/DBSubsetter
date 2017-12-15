package load.physics

import e2e.AbstractPostgresqlEndToEndTest
import load.LoadTest

import scala.sys.process._

class PhysicsPostgresqlTest extends AbstractPostgresqlEndToEndTest with PhysicsTestCases with LoadTest {
  override val originPort = 5573
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.scientists ::: id in (2, 3, 4, 5, 6) ::: includeChildren",
    //    TODO: fix so that some experiment plans have no scientist. Then use this base query to test auto-skipPkStore calculations
    //    "--baseQuery", "public.experiment_plans ::: id % 35 = 0 ::: includeChildren",
    "--skipPkStore", "public.datum_note_responses",
    "--skipPkStore", "public.datum_notes",
    "--skipPkStore", "public.gravitational_wave_data",
    "--skipPkStore", "public.particle_collider_data",
    "--skipPkStore", "public.quantum_data"
  )

  override def createOriginDb(): Unit = s"docker start physics_origin_postgres".!

  override def setupDDL(): Unit = {}

  override def setupDML(): Unit = {}

  override val singleThreadedRuntimeThreshold: Long = 400000

  override val akkaStreamsRuntimeThreshold: Long = 2
}

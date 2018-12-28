package load.physics

import e2e.AbstractPostgresqlEndToEndTest

import scala.sys.process._

class PhysicsPostgresqlTest extends AbstractPostgresqlEndToEndTest with PhysicsTestCases {
  override protected val originPort = 5573

  override protected val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.scientists ::: id in (2) ::: includeChildren",
    //    TODO: fix so that some experiment plans have no scientist. Then use this base query to test auto-skipPkStore calculations
    //    "--baseQuery", "public.experiment_plans ::: id % 35 = 0 ::: includeChildren",
    "--excludeTable", "public.particle_domain",
    "--excludeTable", "public.quantum_domain",
    "--excludeTable", "public.gravitational_wave_domain",
    "--skipPkStore", "public.datum_note_responses",
    "--skipPkStore", "public.datum_notes",
    "--skipPkStore", "public.gravitational_wave_data",
    "--skipPkStore", "public.particle_collider_data",
    "--skipPkStore", "public.quantum_data"
  )

  override protected def prepareTargetDDL(): Unit = {
    super.prepareTargetDDL()
    "./src/test/scala/load/physics/copy_domain_data_postgres.sh".!
  }

// TODO: put back when we reintroduce load tests
//  override val singleThreadedRuntimeThreshold: Long = 400000
//
//  override val akkaStreamsRuntimeThreshold: Long = 2600000
}

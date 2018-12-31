package load.physics

import e2e.{AbstractPostgresqlEndToEndTest, PostgresqlEndToEndTestUtil, SharedTestContainers}
import load.LoadTest
import util.Ports
import util.db.{DatabaseContainer, DatabaseContainerSet, PostgreSQLContainer, PostgreSQLDatabase}
import util.docker.ContainerUtil

import scala.sys.process._

class PhysicsTestPostgreSQL extends AbstractPostgresqlEndToEndTest with LoadTest[PostgreSQLDatabase] with PhysicsTest {
  override val singleThreadedRuntimeLimitMillis: Long = 40000

  override val akkaStreamsRuntimeLimitMillis: Long = 2600000

  private lazy val mustReCreateOriginDb: Boolean = true // TODODOODO !ContainerUtil.exists(containers.origin.name)

  /*
    * Only to be used when manually changing the origin db definition. In this case, the origin DB needs
    * to be completely rebuilt from scratch using Slick definitions, as opposed to being efficiently
    * loaded from an existing dump file. Only set this to `true` if you really know what you are doing, and
    * if you are prepared to make sure the dump file stored in S3 gets updated to your latest version.
    */
  private val skipOriginDbPerformanceOptimization: Boolean = false

  override protected def startOriginContainer(): Unit = {
    if (mustReCreateOriginDb) {
      DatabaseContainer.recreatePostgreSQL(containers.origin.name, containers.origin.db.port)
    } else {
      ContainerUtil.start(containers.origin.name)
    }
  }

  override protected def startTargetContainers(): Unit = SharedTestContainers.postgres

  override protected def createOriginDatabase(): Unit = {
    if (mustReCreateOriginDb) {
      PostgresqlEndToEndTestUtil.createDb(containers.origin.name, containers.origin.db.name)
    }
  }

  override protected def containers: DatabaseContainerSet[PostgreSQLDatabase] = {
    val defaults = super.containers

    val originDb = new PostgreSQLDatabase("physics_db", Ports.postgresPhysicsDbOrigin)
    val originContainer = new PostgreSQLContainer("physics_origin_postgres", originDb)

    new DatabaseContainerSet[PostgreSQLDatabase](
      originContainer,
      defaults.targetSingleThreaded,
      defaults.targetAkkaStreams
    )
  }

  override protected def prepareOriginDDL(): Unit = {
    (mustReCreateOriginDb, skipOriginDbPerformanceOptimization) match {
      case (false, _) => // No action necessary
      case (true, false) => // Load origin DB from dump file stored in S3
        val dumpUrl = "https://s3.amazonaws.com/db-subsetter/load-test/physics-db/pgdump.sql.gz"
        s"./src/test/util/load_postgres_db_from_s3.sh $dumpUrl ${containers.origin.name} ${containers.origin.db.name}".!!
      case (true, true) => // Recreate origin DB from original slick definitions
        super.prepareOriginDDL()
    }
  }

  override protected def prepareOriginDML(): Unit = {
    (mustReCreateOriginDb, skipOriginDbPerformanceOptimization) match {
      case (false, _) => // No action necessary
      case (true, false) => // No action necessary (already done in prepareOriginDDL)
      case (true, true) => super.prepareOriginDML() // We have to populate it from scratch
    }
    println("Done with prepareOriginDML")
  }

  override protected def prepareTargetDDL(): Unit = {
    super.prepareTargetDDL()
    /*
     * Copying domain data to compliment the --excludeTable option
     */
    s"./src/test/scala/load/physics/copy_domain_data_postgres.sh ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/scala/load/physics/copy_domain_data_postgres.sh ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
  }

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

}

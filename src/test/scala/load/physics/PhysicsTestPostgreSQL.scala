//package load.physics
//
//import e2e.{PostgresEnabledTest, PostgresqlEndToEndTestUtil, SharedTestContainers}
//import load.LoadTest
//import util.Ports
//import util.db.{DatabaseSet, PostgreSQLContainer, PostgreSQLDatabase}
//import util.docker.ContainerUtil
//
//import scala.sys.process._
//
//class PhysicsTestPostgreSQL extends PostgresEnabledTest with LoadTest[PostgreSQLDatabase] with PhysicsTest {
//  override val singleThreadedRuntimeLimitMillis: Long = 13000000 // 3.6 hours
//
//  override val akkaStreamsRuntimeLimitMillis: Long = 2700000 // 45 minutes
//
//  private lazy val mustReCreateOriginDb: Boolean = !ContainerUtil.exists(dbs.origin.name)
//
//  /*
//    * Only to be used when manually changing the origin db definition. In this case, the origin DB needs
//    * to be completely rebuilt from scratch using Slick definitions, as opposed to being efficiently
//    * loaded from an existing dump file. Only set this to `true` if you really know what you are doing, and
//    * if you are prepared to make sure the dump file stored in S3 gets updated to your latest version.
//    */
//  private val skipOriginDbPerformanceOptimization: Boolean = false
//
//  override protected def startOriginContainer(): Unit = {
//    if (mustReCreateOriginDb) {
//      // Commenting out to enable drone e2e tests
//      // DatabaseContainer.recreatePostgreSQL(containers.origin.name, containers.origin.db.port)
//    } else {
//      ContainerUtil.start(dbs.origin.name)
//    }
//  }
//
//  override protected def startTargetContainers(): Unit = SharedTestContainers.postgres
//
//  override protected def createOriginDatabase(): Unit = {
//    if (mustReCreateOriginDb) {
//      PostgresqlEndToEndTestUtil.createDb(dbs.origin.db)
//    }
//  }
//
//  override protected def dbs: DatabaseSet[PostgreSQLDatabase] = {
//    val defaults = super.dbs
//
//    val originDb = new PostgreSQLDatabase("localhost", Ports.postgresPhysicsDbOrigin, "physics_db")
//    val originContainer = new PostgreSQLContainer("physics_origin_postgres", originDb)
//
//    new DatabaseSet[PostgreSQLDatabase](
//      originContainer,
//      defaults.targetSingleThreaded,
//      defaults.targetAkkaStreams
//    )
//  }
//
//  override protected def prepareOriginDDL(): Unit = {
//    (mustReCreateOriginDb, skipOriginDbPerformanceOptimization) match {
//      case (false, _) => // No action necessary
//      case (true, false) => // Load origin DB from dump file stored in S3
//        val dumpUrl = "https://s3.amazonaws.com/db-subsetter/load-test/physics-db/pgdump.sql.gz"
//        s"./src/test/util/load_postgres_db_from_s3.sh $dumpUrl ${dbs.origin.name} ${dbs.origin.db.name}".!!
//      case (true, true) => // Recreate origin DB from original slick definitions
//        super.prepareOriginDDL()
//    }
//  }
//
//  override protected def prepareOriginDML(): Unit = {
//    (mustReCreateOriginDb, skipOriginDbPerformanceOptimization) match {
//      case (false, _) => // No action necessary
//      case (true, false) => // No action necessary (already done in prepareOriginDDL)
//      case (true, true) =>
//        super.prepareOriginDML() // We have to populate it from scratch then dump it out and store in S3
//        // Command to dump it out (this is inside a docker container so you will need to hack around the volume mounting to get the files out)
//        // $ docker exec physics_origin_postgres pg_dump --format directory --jobs 8 --compress 9 --dbname physics_db --user postgres --file /var/lib/postgresql/data/physics-db-dump
//        // $ # Move files from docker volume location into host machine-accessible area
//        // $ tar -c -v -f physics-db-dump.tar physics-db-dump
//    }
//  }
//
//  override protected def prepareTargetDDL(): Unit = {
//    super.prepareTargetDDL()
//    /*
//     * Copying domain data to compliment the --excludeTable option
//     */
//    s"./src/test/scala/load/physics/copy_domain_data_postgres.sh ${dbs.origin.name} ${dbs.origin.db.name} ${dbs.targetSingleThreaded.name} ${dbs.targetSingleThreaded.db.name}".!!
//    s"./src/test/scala/load/physics/copy_domain_data_postgres.sh ${dbs.origin.name} ${dbs.origin.db.name} ${dbs.targetAkkaStreams.name} ${dbs.targetAkkaStreams.db.name}".!!
//  }
//
//  override protected val programArgs = Array(
//    "--schemas", "public",
//    "--baseQuery", "public.scientists ::: id in (2) ::: includeChildren",
//    //    TODO: fix so that some experiment plans have no scientist. Then consider using this base query.
//    //    "--baseQuery", "public.experiment_plans ::: id % 35 = 0 ::: includeChildren",
//    "--excludeTable", "public.particle_domain",
//    "--excludeTable", "public.quantum_domain",
//    "--excludeTable", "public.gravitational_wave_domain",
//  )
//}

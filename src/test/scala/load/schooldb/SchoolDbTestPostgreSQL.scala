package load.schooldb

import e2e.{AbstractPostgresqlEndToEndTest, PostgresqlEndToEndTestUtil}
import load.LoadTest
import util.Ports
import util.db.{DatabaseContainer, DatabaseContainerSet, PostgreSQLContainer, PostgreSQLDatabase}
import util.docker.ContainerUtil

import scala.sys.process._

class SchoolDbTestPostgreSQL extends AbstractPostgresqlEndToEndTest with LoadTest[PostgreSQLDatabase] with SchoolDbTest {

  override val singleThreadedRuntimeLimitMillis: Long = 220000

  override val akkaStreamsRuntimeLimitMillis: Long = 25000

  private lazy val mustReCreateOriginDb: Boolean = true // !ContainerUtil.exists(containers.origin.name)

  /*
   * Only to be used when changing the origin db definition. Otherwise we can just load the existing dump from S3
   */
//  private val populateOriginFromScratch: Boolean = false

  override protected def startOriginContainer(): Unit = {
    if (mustReCreateOriginDb) {
      DatabaseContainer.recreatePostgreSQL(containers.origin.name, containers.origin.db.port)
    } else {
      ContainerUtil.start(containers.origin.name)
    }
  }

  override protected def createOriginDatabase(): Unit = {
    if (mustReCreateOriginDb) {
      PostgresqlEndToEndTestUtil.createDb(containers.origin.name, containers.origin.db.name)
    }
  }

  override protected def containers: DatabaseContainerSet[PostgreSQLDatabase] = {
    val defaults = super.containers

    val originDb = new PostgreSQLDatabase("school_db", Ports.postgresSchoolDbOrigin)
    val originContainer = new PostgreSQLContainer("school_db_origin_postgres", originDb)

    new DatabaseContainerSet[PostgreSQLDatabase](
      originContainer,
      defaults.targetSingleThreaded,
      defaults.targetAkkaStreams
    )
  }

  override protected def prepareOriginDDL(): Unit = {
    if (mustReCreateOriginDb) {
      val dumpUrl = "https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz"
      s"./src/test/util/blarghy_mc_blargh_face.sh $dumpUrl ${containers.origin.name} ${containers.origin.db.name}".!!
//      (dumpUrl #> "gunzip" #| s"docker exec -it ${containers.origin.name} psql --user postgres").!!
//      val createSchemaStatements: DBIO[Unit] = DBIO.seq(
//        sqlu"create schema school_db",
//        sqlu"""create schema "Audit""""
//      )
//      Await.ready(originSlick.run(createSchemaStatements), Duration.Inf)
//      super.prepareOriginDDL()
    }
  }

  override protected def prepareOriginDML(): Unit = {} // No-op (already taken care of in prepareOriginDDL)

  override protected val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )
}

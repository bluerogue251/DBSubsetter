package load.physics

import e2e.AbstractPostgresqlEndToEndTest
import slick.jdbc.PostgresProfile.api._
import util.Ports
import util.db.{DatabaseContainerSet, PostgreSQLContainer, PostgreSQLDatabase}
import util.docker.ContainerUtil


// Assumes physics DB is completely set up already
class TargetDBInsertBenchmarkPostgreSQL extends AbstractPostgresqlEndToEndTest {

  override protected def startOriginContainer(): Unit = ContainerUtil.start(containers.origin.name)

  override protected def createOriginDatabase(): Unit = {}

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

  override protected def prepareOriginDDL(): Unit = {}

  override protected def prepareOriginDML(): Unit = {}

  override protected def prepareTargetDDL(): Unit = {
    val singleRowPerInsert = sqlu"create table single_row_per_insert(id bigserial primary key, name varchar not null)"
    val jdbcBatchInsert = sqlu"create table jdbc_batch_insert(id bigserial primary key, name varchar not null)"
    // Could also consider a jdbcBatchInsert starting with just primary keys, and fetching row data later
    val bulkCopyInsert = sqlu"create table bulk_copy_insert(id bigserial primary key, name varchar not null)"
    val createTableStatements = DBIO.seq(
      singleRowPerInsert,
      jdbcBatchInsert,
      bulkCopyInsert
    )
    targetSingleThreadedSlick.run(createTableStatements)
  }

  protected def programArgs: Array[String]

  override protected def runSubsetInSingleThreadedMode(): Unit = {}

  override protected def runSubsetInAkkaStreamsMode(): Unit = {}

  override protected def postSubset(): Unit = {}
}

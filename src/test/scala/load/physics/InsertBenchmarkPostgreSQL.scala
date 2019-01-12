package load.physics

import e2e.AbstractPostgresqlEndToEndTest
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlAction
import util.Ports
import util.db.{DatabaseContainerSet, PostgreSQLContainer, PostgreSQLDatabase}
import util.docker.ContainerUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration


// Assumes physics DB is completely set up already
class InsertBenchmarkPostgreSQL extends AbstractPostgresqlEndToEndTest {

  override protected def testName: String = "insert_benchmark"

  protected def programArgs: Array[String] = ???

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
    super.prepareTargetDDL()

    val jdbcBatch100 = createTargetTableSql("jdbcBatch100")
    val jdbcBatch1000 = createTargetTableSql("jdbcBatch1000")
    val jdbcBatch10000 = createTargetTableSql("jdbcBatch10000")
    val singleStatement100 = createTargetTableSql("singleStatement100")
    val singleStatement1000 = createTargetTableSql("singleStatement1000")
    val singleStatement10000 = createTargetTableSql("singleStatement10000")
    val bulkCopy100 = createTargetTableSql("bulkCopy100")
    val bulkCopy1000 = createTargetTableSql("bulkCopy1000")
    val bulkCopy10000 = createTargetTableSql("bulkCopy10000")
    val createTableStatements = DBIO.seq(
      jdbcBatch100,
      jdbcBatch1000,
      jdbcBatch10000,
      singleStatement100,
      singleStatement1000,
      singleStatement10000,
      bulkCopy100,
      bulkCopy1000,
      bulkCopy10000
    )

    Await.ready(targetSingleThreadedSlick.run(createTableStatements), Duration.Inf)
  }

  override protected def runSubsetInSingleThreadedMode(): Unit = {}

  override protected def runSubsetInAkkaStreamsMode(): Unit = {}

  override protected def postSubset(): Unit = {}

  test("JDBC Batch Insert 100 Rows At A Time") {
    System.out.println("woot")
    assert(1 === 2)
  }

  test("JDBC Batch Insert 1000 Rows At A Time") {

  }

  test("JDBC Batch Insert 10000 Rows At A Time") {

  }

  test("Single Statement Insert 100 Rows At A Time") {

  }

  test("Single Statement Insert 1000 Rows At A Time") {

  }

  test("Single Statement Insert 10000 Rows At A Time") {

  }

  test("Bulk Copy Insert 100 Rows At A Time") {

  }

  test("Bulk Copy Insert 1000 Rows At A Time") {

  }

  test("Bulk Copy Insert 10000 Rows At A Time") {

  }

  private[this] def timeMillis(f: () => Unit): Long = ???

  private[this] def createTargetTableSql(suffix: String): SqlAction[Int, NoStream, Effect] = {
    sqlu"""create table quantum_data_$suffix(
             id bigserial primary key,
             experiment_id integer not null references experiments(id),
             quantum_domain_data_id bigint not null references quantum_domain(id),
             data_1 varchar not null,
             data_2 varchar not null,
             data_3 varchar not null,
             created_at timestamp without time zone not null
          )"""
  }
}

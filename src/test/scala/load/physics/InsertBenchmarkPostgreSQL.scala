package load.physics

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import e2e.AbstractPostgresqlEndToEndTest
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlAction
import trw.dbsubsetter.db.Row
import util.Ports
import util.db.{DatabaseContainerSet, PostgreSQLContainer, PostgreSQLDatabase}
import util.docker.ContainerUtil

import scala.collection.mutable.ArrayBuffer
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

    // TODO all of these are single connection at a time. What about multiple connections at a time?
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
    val batchSize = 100
    val timeMillis: Long = jdbcBatchFullFlow(batchSize)
    System.out.println(s"JDBC Batch Insert 100 Rows At A Time Runtime: $timeMillis Seconds")
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

  private[this] def jdbcBatchFullFlow(table: String, batchSize: Int): Long = {
    val insertStatement: PreparedStatement = buildInsertStatement(table)
    runWithTimerSeconds(() => {
      (0 to 6000005 by batchSize).foreach(startOfBatchId => {
        val rows: Vector[Row] = fetchRows(startOfBatchId, startOfBatchId + batchSize - 1)
        jdbcBatchInsert(insertStatement, rows)
      })
    })
  }

  private[this] def runWithTimerSeconds(f: () => Unit): Long = {
    val start: Long = System.nanoTime()
    f.apply()
    val end: Long = System.nanoTime()
    val durationNanos = end - start
    val durationSeconds = durationNanos / 1000000000
    durationSeconds
  }

  private[this] def createTargetTableSql(suffix: String): SqlAction[Int, NoStream, Effect] = {
    sqlu"""create table quantum_data_#$suffix(
             id bigserial primary key,
             experiment_id integer not null,
             quantum_domain_data_id bigint not null,
             data_1 varchar not null,
             data_2 varchar not null,
             data_3 varchar not null,
             created_at timestamp without time zone not null
          )"""
  }

  private[this] lazy val selectStatement = {
    originJdbcConnection.prepareStatement("select * from quantum_data where id between ? and ?")
  }

  private[this] def fetchRows(start: Int, end: Int): Vector[Row] = {
    selectStatement.clearParameters()
    selectStatement.setObject(1, start)
    selectStatement.setObject(2, end)
    val resultSet: ResultSet = selectStatement.executeQuery()

    val rows = ArrayBuffer.empty[Row]
    while (resultSet.next()) {
      val row: Row = Array(
        resultSet.getObject(1),
        resultSet.getObject(2),
        resultSet.getObject(3),
        resultSet.getObject(4),
        resultSet.getObject(5),
        resultSet.getObject(6),
        resultSet.getObject(7)
      )
      rows += row
    }
    rows.toVector
  }

  private[this] def buildInsertStatement(table: String): PreparedStatement = {
    val insertSql =
      s"""insert into $table
          |  (id, experiment_id, quantum_domain_data_id, data_1, data_2, data_3, created_at)
          |  values
          |  (?, ?, ?, ?, ?, ?, ?)
       """.stripMargin
    targetJdbcConnection.prepareStatement(insertSql)
  }

  private[this] def jdbcBatchInsert(insertStatement: PreparedStatement, rows: Vector[Row]): Unit = {
    insertStatement.clearParameters()

    rows.foreach { row =>
      insertStatement.setObject(1, row(0))
      insertStatement.setObject(2, row(1))
      insertStatement.setObject(3, row(2))
      insertStatement.setObject(4, row(3))
      insertStatement.setObject(5, row(4))
      insertStatement.setObject(6, row(5))
      insertStatement.setObject(7, row(6))
      insertStatement.addBatch()
    }

    insertStatement.executeBatch()
  }

  private[this] lazy val originJdbcConnection: Connection = {
    val connectionString = s"jdbc:postgresql://localhost:${containers.origin.db.port}/${containers.origin.db.name}?user=postgres"
    val connection: Connection = DriverManager.getConnection(connectionString)
    connection.setReadOnly(true)
    connection
  }

  private[this] lazy val targetJdbcConnection: Connection = {
    val connectionString = s"jdbc:postgresql://localhost:${containers.targetSingleThreaded.db.port}/${containers.targetSingleThreaded.db.name}?user=postgres"
    DriverManager.getConnection(connectionString)
  }
}

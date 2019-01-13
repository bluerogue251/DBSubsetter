package load.physics

import java.io._
import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import e2e.AbstractPostgresqlEndToEndTest
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlAction
import trw.dbsubsetter.db.Row
import util.Ports
import util.db.{DatabaseContainerSet, PostgreSQLContainer, PostgreSQLDatabase}
import util.docker.ContainerUtil

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


// Assumes physics DB is completely set up already
// By far the fastest is when we use Bulk Copy with a small SQL Query String
// But doing a Bulk Copy with a large SQL query string with many IDs in it is almost just as slow as the single SQL Statement Solution
// (But, it is nice not to have to load all those rows into memory...)
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
    val jdbcBatch100 = createTargetTableSql("jdbc_batch_100")
    val jdbcBatch1000 = createTargetTableSql("jdbc_batch_1000")
    val jdbcBatch10000 = createTargetTableSql("jdbc_batch_10000")
    val singleStatement100 = createTargetTableSql("single_statement_100")
    val singleStatement1000 = createTargetTableSql("single_statement_1000")
    val singleStatement4681 = createTargetTableSql("single_statement_4681")
    val bulkCopy100 = createTargetTableSql("bulk_copy_100")
    val bulkCopy1000 = createTargetTableSql("bulk_copy_1000")
    val bulkCopy4681 = createTargetTableSql("bulk_copy_4681")
    val bulkCopy10000 = createTargetTableSql("bulk_copy_10000")
    val bulkCopy50000 = createTargetTableSql("bulk_copy_50000")
    val createTableStatements = DBIO.seq(
      jdbcBatch100,
      jdbcBatch1000,
      jdbcBatch10000,
      singleStatement100,
      singleStatement1000,
      singleStatement4681,
      bulkCopy100,
      bulkCopy1000,
      bulkCopy4681,
      bulkCopy10000,
      bulkCopy50000
    )

    Await.ready(targetSingleThreadedSlick.run(createTableStatements), Duration.Inf)
  }

  override protected def runSubsetInSingleThreadedMode(): Unit = {}

  override protected def runSubsetInAkkaStreamsMode(): Unit = {}

  override protected def postSubset(): Unit = {}

  test("JDBC Batch Insert 100 Rows At A Time") {
    jdbcBatchFullFlow("jdbc_batch_100", 100)
  }

  test("JDBC Batch Insert 1000 Rows At A Time") {
    jdbcBatchFullFlow("jdbc_batch_1000", 1000)
  }

  test("JDBC Batch Insert 10000 Rows At A Time") {
    jdbcBatchFullFlow("jdbc_batch_10000", 10000)
  }

  test("Single Statement Insert 100 Rows At A Time") {
    singleStatementFullFlow("single_statement_100", 100)

  }

  test("Single Statement Insert 1000 Rows At A Time") {
    singleStatementFullFlow("single_statement_1000", 1000)
  }

  /*
   * Prepared Statement uses the positive half of a signed 2-byte integer so that max is:
   * 2 bytes = 16 bits
   * 16 bits signed = 15 bits of positive numbers
   * 2^15 possible placeholders = 32768 possible placeholders
   * 7 values per row so 32768 / 7 = A Max of 4681 rows at a time
   */
  test("Single Statement Insert 4681 Rows At A Time") {
    singleStatementFullFlow("single_statement_4681", 4681)
  }

  test("Bulk Copy 100 Rows At A Time") {
    bulkCopyFullFlow("bulk_copy_100", 100)
  }

  test("Bulk Copy 1000 Rows At A Time") {
    bulkCopyFullFlow("bulk_copy_1000", 1000)
  }

  test("Bulk Copy 4681 Rows At A Time") {
    bulkCopyFullFlow("bulk_copy_4681", 4681)
  }

  test("Bulk Copy 10000 Rows At A Time") {
    bulkCopyFullFlow("bulk_copy_10000", 10000)
  }

  test("Bulk Copy 50000 Rows At A Time") {
    bulkCopyFullFlow("bulk_copy_50000", 50000)
  }

  private[this] def jdbcBatchFullFlow(tableSuffix: String, batchSize: Int): Unit = {
    def buildInsertStatement(tableSuffix: String): PreparedStatement = {
      val insertSql =
        s"""insert into quantum_data_$tableSuffix
           |  (id, experiment_id, quantum_domain_data_id, data_1, data_2, data_3, created_at)
           |  values
           |  (?, ?, ?, ?, ?, ?, ?)
       """.stripMargin
      targetJdbcConnection.prepareStatement(insertSql)
    }

    def insertRows(insertStatement: PreparedStatement, rows: Vector[Row]): Unit = {
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
      targetJdbcConnection.commit()
    }

    val insertStatement: PreparedStatement = buildInsertStatement(tableSuffix)
    val runtimeSeconds: Long = runWithTimerSeconds(() => {
      (1 to 6000000 by batchSize).foreach(startOfBatchId => {
        val rows: Vector[Row] = fetchRows(startOfBatchId, startOfBatchId + batchSize - 1)
        insertRows(insertStatement, rows)
      })
    })
    System.out.println(s"JDBC Batch Insert $batchSize Rows At A Time Runtime: $runtimeSeconds Seconds")
  }

  private[this] def singleStatementFullFlow(tableSuffix: String, batchSize: Int): Unit = {
    def buildInsertStatement(batchSizeInternal: Int): PreparedStatement = {
      val questionMarks = (0 until batchSizeInternal).map(_ => "(?, ?, ?, ?, ?, ?, ?)").mkString(",\n")
      val insertSql =
        s"""insert into quantum_data_$tableSuffix
           |  (id, experiment_id, quantum_domain_data_id, data_1, data_2, data_3, created_at)
           |  values
           |  $questionMarks
       """.stripMargin
      targetJdbcConnection.prepareStatement(insertSql)
    }

    def insertRows(insertStatement: PreparedStatement, rows: Vector[Row]): Unit = {
      insertStatement.clearParameters()

      rows.zipWithIndex.foreach { case (row, rowIndex) =>
        insertStatement.setObject(rowIndex * 7 + 1, row(0))
        insertStatement.setObject(rowIndex * 7 + 2, row(1))
        insertStatement.setObject(rowIndex * 7 + 3, row(2))
        insertStatement.setObject(rowIndex * 7 + 4, row(3))
        insertStatement.setObject(rowIndex * 7 + 5, row(4))
        insertStatement.setObject(rowIndex * 7 + 6, row(5))
        insertStatement.setObject(rowIndex * 7 + 7, row(6))
      }

      insertStatement.execute()
      targetJdbcConnection.commit()
    }

    val defaultInsertStatement: PreparedStatement = buildInsertStatement(batchSize)
    val runtimeSeconds: Long = runWithTimerSeconds(() => {
      (1 to 6000000 by batchSize).foreach(startOfBatchId => {
        val rows: Vector[Row] = fetchRows(startOfBatchId, startOfBatchId + batchSize - 1)
        val insertStatement: PreparedStatement =
          if (rows.length == batchSize) {
            defaultInsertStatement
          } else {
            // This should only happen for the last batch
            System.out.println(s"Building single insert statement for nonstandard batch size ${rows.length}")
            buildInsertStatement(rows.length)
          }
        insertRows(insertStatement, rows)
      })
    })
    System.out.println(s"Single Insert Statement $batchSize Rows At A Time Runtime: $runtimeSeconds Seconds")
  }

  private[this] def bulkCopyFullFlow(tableSuffix: String, batchSize: Int): Unit = {
    val originCopyManager: CopyManager = new CopyManager(originJdbcConnection.asInstanceOf[BaseConnection])
    val targetCopyManager: CopyManager = new CopyManager(targetJdbcConnection.asInstanceOf[BaseConnection])


    def makeBulkCopyIdString(fromIdInclusive: Int, endIdInclusive: Int): String = {
      val idValues = (fromIdInclusive to endIdInclusive).mkString(",")
      s"COPY (select * from quantum_data where id in ($idValues)) TO STDOUT (FORMAT BINARY)"
    }

    def doPostgresBulkCopy(fromIdInclusive: Int, toIdInclusive: Int): Unit = {
      // The targetWriteStream should read its input from the originReadStream
      // See https://stackoverflow.com/a/23874232
      val originReadStream: java.io.PipedOutputStream = new PipedOutputStream()
      val targetWriteStream: java.io.PipedInputStream = new PipedInputStream(originReadStream)

      import scala.concurrent.ExecutionContext.Implicits.global
      Future {
        val copyFromOriginSql = makeBulkCopyIdString(fromIdInclusive, toIdInclusive)
        originCopyManager.copyOut(copyFromOriginSql, originReadStream)
        originReadStream.close()
      }

      val copyToTargetSql = s"COPY quantum_data_$tableSuffix FROM STDIN (FORMAT BINARY)"
      targetCopyManager.copyIn(copyToTargetSql, targetWriteStream)
      targetWriteStream.close()
    }

    val runtimeSeconds: Long = runWithTimerSeconds(() => {
      (1 to 6000000 by batchSize).foreach(startOfBatchId => {
        doPostgresBulkCopy(startOfBatchId, startOfBatchId + batchSize - 1)
        targetJdbcConnection.commit()
      })
    })

    System.out.println(s"Bulk Copy $batchSize Rows At A Time Runtime: $runtimeSeconds Seconds")
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

  private[this] lazy val originJdbcConnection: Connection = {
    val connectionString = s"jdbc:postgresql://localhost:${containers.origin.db.port}/${containers.origin.db.name}?user=postgres"
    val connection: Connection = DriverManager.getConnection(connectionString)
    connection.setReadOnly(true)
    connection
  }

  private[this] lazy val targetJdbcConnection: Connection = {
    val connectionString = s"jdbc:postgresql://localhost:${containers.targetSingleThreaded.db.port}/${containers.targetSingleThreaded.db.name}?user=postgres"
    val connection: Connection = DriverManager.getConnection(connectionString)
    connection.setAutoCommit(false)
    connection
  }
}

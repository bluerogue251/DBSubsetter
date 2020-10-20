package load.physics

import java.io._
import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import e2e.PostgresEnabledTest
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlAction
import trw.dbsubsetter.db.Row
import util.db.{DatabaseSet, PostgresDatabase}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/*
 * This is a benchmark to find out what the fastest way to insert large volumes of data into Postgres is.
 * This is a very hacky extension of EndToEndTest class, and as benchmarks go it is not carefully controlled.
 * However, it gets the job done for now and can be refactored and improved later.
 *
 * Assumes origin physics DB is completely set up already
 *
 * By far the fastest is when we use Bulk Copy with a small SQL Query String. Unfortunately,
 * our actual bulk copy SQL Query Strings need to be large with many IDs in them. If we go with Bulk Copy
 * as the Production implementation of target DB Inserts, it might be worth having a feature which attempts
 * to batch large lists of sequential integers as ranges with a start and an end, so that we can use a `BETWEEN`
 * clause instead of listing out all of the IDs in the SQL query.
 *
 * Insert Benchmarks for 6,000,000 Rows:
 *
 *   100 Rows at a time
 *   JDBC Batch Insert:       529 Seconds
 *   Single Insert Statement: 495 Seconds
 *   Bulk Copy:               509 Seconds
 *
 *   1000 Rows at a time
 *   JDBC Batch Insert:       182 Seconds
 *   Single Insert Statement: 143 Seconds
 *   Bulk Copy:               91  Seconds
 *
 *   4681 Rows At A Time
 *   JDBC Batch Insert:       121 Seconds
 *   Single Insert Statement: 67  Seconds
 *   Bulk Copy:               53  Seconds
 *
 *   10000 Rows at a time:
 *   JDBC Batch Insert: 118 Seconds
 *   Bulk Copy:         49  Seconds

 */
class InsertBenchmarkPostgres extends PostgresEnabledTest {

  override protected def testName: String = "insert_benchmark"

  protected def programArgs: Array[String] = ???

  override protected def createOriginDatabase(): Unit = {}

  override protected def dbs: DatabaseSet[PostgresDatabase] = {
    val defaults = super.dbs

    val originDb = new PostgresDatabase("localhost", ???, "physics_db")

    new DatabaseSet[PostgresDatabase](
      originDb,
      defaults.target
    )
  }

  override protected def prepareOriginDDL(): Unit = {}

  override protected def prepareOriginDML(): Unit = {}

  override protected def prepareTargetDDL(): Unit = {
    super.prepareTargetDDL()

    // TODO all of these are single connection at a time. What about multiple connections at a time?
    val jdbcBatch100 = createTargetTableSql("jdbc_batch_100")
    val jdbcBatch1000 = createTargetTableSql("jdbc_batch_1000")
    val jdbcBatch4681 = createTargetTableSql("jdbc_batch_4681")
    val jdbcBatch10000 = createTargetTableSql("jdbc_batch_10000")
    val singleStatement100 = createTargetTableSql("single_statement_100")
    val singleStatement1000 = createTargetTableSql("single_statement_1000")
    val singleStatement4681 = createTargetTableSql("single_statement_4681")
    val bulkCopy100 = createTargetTableSql("bulk_copy_100")
    val bulkCopy1000 = createTargetTableSql("bulk_copy_1000")
    val bulkCopy4681 = createTargetTableSql("bulk_copy_4681")
    val bulkCopy10000 = createTargetTableSql("bulk_copy_10000")
    val createTableStatements = DBIO.seq(
      jdbcBatch100,
      jdbcBatch1000,
      jdbcBatch4681,
      jdbcBatch10000,
      singleStatement100,
      singleStatement1000,
      singleStatement4681,
      bulkCopy100,
      bulkCopy1000,
      bulkCopy4681,
      bulkCopy10000
    )

    Await.ready(targetSlick.run(createTableStatements), Duration.Inf)
  }

  test("JDBC Batch Insert 100 Rows At A Time") {
    jdbcBatchFullFlow("jdbc_batch_100", 100)
  }

  test("JDBC Batch Insert 1000 Rows At A Time") {
    jdbcBatchFullFlow("jdbc_batch_1000", 1000)
  }

  test("JDBC Batch Insert 4681 Rows At A Time") {
    jdbcBatchFullFlow("jdbc_batch_4681", 4681)
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
        insertStatement.setObject(1, row.data(0))
        insertStatement.setObject(2, row.data(1))
        insertStatement.setObject(3, row.data(2))
        insertStatement.setObject(4, row.data(3))
        insertStatement.setObject(5, row.data(4))
        insertStatement.setObject(6, row.data(5))
        insertStatement.setObject(7, row.data(6))
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
        insertStatement.setObject(rowIndex * 7 + 1, row.data(0))
        insertStatement.setObject(rowIndex * 7 + 2, row.data(1))
        insertStatement.setObject(rowIndex * 7 + 3, row.data(2))
        insertStatement.setObject(rowIndex * 7 + 4, row.data(3))
        insertStatement.setObject(rowIndex * 7 + 5, row.data(4))
        insertStatement.setObject(rowIndex * 7 + 6, row.data(5))
        insertStatement.setObject(rowIndex * 7 + 7, row.data(6))
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
      val data: Array[Any] = Array(
        resultSet.getObject(1),
        resultSet.getObject(2),
        resultSet.getObject(3),
        resultSet.getObject(4),
        resultSet.getObject(5),
        resultSet.getObject(6),
        resultSet.getObject(7)
      )
      rows += new Row(data)
    }
    rows.toVector
  }

  private[this] lazy val originJdbcConnection: Connection = {
    val connectionString = s"jdbc:postgresql://localhost:${dbs.origin.port}/${dbs.origin.name}?user=postgres"
    val connection: Connection = DriverManager.getConnection(connectionString)
    connection.setReadOnly(true)
    connection
  }

  private[this] lazy val targetJdbcConnection: Connection = {
    val connectionString =
      s"jdbc:postgresql://localhost:${dbs.target.port}/${dbs.target.name}?user=postgres"
    val connection: Connection = DriverManager.getConnection(connectionString)
    connection.setAutoCommit(false)
    connection
  }
}

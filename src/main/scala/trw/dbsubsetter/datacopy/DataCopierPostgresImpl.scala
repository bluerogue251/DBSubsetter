package trw.dbsubsetter.datacopy

import org.postgresql.copy.CopyManager
import trw.dbsubsetter.db.{Constants, DbAccessFactory, SchemaInfo}

import java.io.{PipedInputStream, PipedOutputStream}
import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

private[datacopy] final class DataCopierPostgresImpl(dbAccessFactory: DbAccessFactory, schemaInfo: SchemaInfo)
    extends DataCopier {

  private[this] val originCopyManager: CopyManager =
    dbAccessFactory.buildOriginPostgresCopyManager()

  private[this] val targetCopyManager: CopyManager =
    dbAccessFactory.buildTargetPostgresCopyManager()

  private[this] val copyOutExecutionContext: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

  def runTask(task: DataCopyTask): Unit = {
    if (!Constants.dataCopyBatchSizes.contains(task.pkValues.size)) {
      throw new IllegalArgumentException(s"Unsupported data copy batch size: ${task.pkValues.size}")
    }

    val allColumnNamesSql: String =
      schemaInfo
        .dataColumnsByTable(task.table)
        .map(col => s""""${col.name}"""")
        .mkString(", ")

    val pkColumnNamesSql: String =
      schemaInfo
        .pksByTable(task.table)
        .columns
        .map(col => s""""${col.name}"""")
        .mkString("(", ",", ")")

    val individualPkValuesSql: Seq[String] =
      task.pkValues
        .map(pkValue => {
          pkValue.x
            .map(quote)
            .mkString("(", ",", ")")
        })

    val allPkValuesSql: String =
      individualPkValuesSql.mkString(",")

    val selectStatement: String =
      s"""
         | COPY (
         |   select $allColumnNamesSql from "${task.table.schema.name}"."${task.table.name}"
         |   where $pkColumnNamesSql in ($allPkValuesSql)
         | )
         | TO STDOUT (FORMAT BINARY)
         | """.stripMargin

    // The targetWriteStream receives data from the originReadStream. See https://stackoverflow.com/a/23874232
    val originReadStream: java.io.PipedOutputStream = new PipedOutputStream()
    val targetWriteStream: java.io.PipedInputStream = new PipedInputStream(originReadStream)

    val future = Future {
      originCopyManager.copyOut(selectStatement, originReadStream)
      originReadStream.close()
    }(copyOutExecutionContext)

    future.failed.foreach(e => throw e)(copyOutExecutionContext)

    val copyToTargetSql =
      s"""
         | COPY "${task.table.schema.name}"."${task.table.name}"($allColumnNamesSql)
         | FROM STDIN (FORMAT BINARY)
         | """.stripMargin

    // Use 4 times the default buffer size
    val bufferSizeInBytes: Int = 262144
    targetCopyManager.copyIn(copyToTargetSql, targetWriteStream, bufferSizeInBytes)
    targetWriteStream.close()
  }

  private[this] def quote(value: Any): String = {
    if (value.isInstanceOf[UUID] || value.isInstanceOf[String]) {
      s"'$value'"
    } else {
      value.toString
    }
  }
}

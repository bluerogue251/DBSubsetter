package trw.dbsubsetter.datacopy.impl

import java.io.{PipedInputStream, PipedOutputStream}
import java.util.UUID
import java.util.concurrent.Executors

import org.postgresql.copy.CopyManager
import trw.dbsubsetter.datacopy.DataCopyWorkflow
import trw.dbsubsetter.db.{Constants, DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow.DataCopyTask

import scala.concurrent.{ExecutionContext, Future}


private[datacopy] final class PostgresOptimizedDataCopyWorkflowImpl(dbAccessFactory: DbAccessFactory, schemaInfo: SchemaInfo) extends DataCopyWorkflow {

  private[this] val originCopyManager: CopyManager =
    dbAccessFactory.buildOriginPostgresCopyManager()

  private[this] val targetCopyManager: CopyManager =
    dbAccessFactory.buildTargetPostgresCopyManager()

  private[this] val copyOutExecutionContext: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

  def process(dataCopyTask: DataCopyTask): Unit = {
    if (!Constants.dataCopyBatchSizes.contains(dataCopyTask.pkValues.size)) {
      throw new IllegalArgumentException(s"Unsupported data copy batch size: ${dataCopyTask.pkValues.size}")
    }

    val allColumnNamesSql: String =
      schemaInfo
      .dataColumnsByTableOrdered(dataCopyTask.table)
      .map(col => s""""${col.name}"""")
      .mkString(", ")

    val pkColumnNamesSql: String =
      schemaInfo
        .pksByTable(dataCopyTask.table)
        .columns
        .map(col => s""""${col.name}"""")
        .mkString("(", ",", ")")

    val individualPkValuesSql: Seq[String] =
      dataCopyTask
        .pkValues
        .map(pkValue => {
          pkValue
            .individualColumnValues
            .map(quote)
            .mkString("(", ",", ")")
        })

    val allPkValuesSql: String =
      individualPkValuesSql.mkString(",")

    val selectStatement: String =
      s"""
         | COPY (
         |   select $allColumnNamesSql from "${dataCopyTask.table.schema.name}"."${dataCopyTask.table.name}"
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
         | COPY "${dataCopyTask.table.schema.name}"."${dataCopyTask.table.name}"($allColumnNamesSql)
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

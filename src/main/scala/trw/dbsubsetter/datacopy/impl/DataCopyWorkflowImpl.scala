package trw.dbsubsetter.datacopy.impl

import trw.dbsubsetter.db.{Constants, DbAccessFactory, PrimaryKeyValue, Row}
import trw.dbsubsetter.workflow.DataCopyTask


final class DataCopyWorkflowImpl(dbAccessFactory: DbAccessFactory) {

  private[this] val originDbAccess = dbAccessFactory.buildOriginDbAccess()

  private[this] val targetDbAccess = dbAccessFactory.buildTargetDbAccess()

  def process(dataCopyTask: DataCopyTask): Unit = {
    val pkValues: Seq[PrimaryKeyValue] = dataCopyTask.pkValues

    if (!Constants.dataCopyBatchSizes.contains(pkValues.size)) {
      throw new IllegalArgumentException(s"Unsupported data copy batch size: ${pkValues.size}")
    }

    val rowsToInsert: Vector[Row] =
      originDbAccess.getRowsFromPrimaryKeyValues(dataCopyTask.table, pkValues)

    if (!rowsToInsert.size.equals(pkValues.size)) {
      val message: String =
        s"Number of rows fetched did not equal number of primary keys. " +
          s"Rows fetched: ${rowsToInsert.size}. " +
          s"Primary keys to fetch by: ${pkValues.size}"
      throw new IllegalStateException(message)
    }

    targetDbAccess.insertRows(dataCopyTask.table, rowsToInsert)
  }
}

package trw.dbsubsetter.datacopy

import trw.dbsubsetter.db.{Constants, DbAccessFactory, MultiColumnPrimaryKeyValue, Row}

private[datacopy] final class DataCopierGenericImpl(dbAccessFactory: DbAccessFactory) extends DataCopier {

  private[this] val originDbAccess = dbAccessFactory.buildOriginDbAccess()

  private[this] val targetDbAccess = dbAccessFactory.buildTargetDbAccess()

  def runTask(task: DataCopyTask): Unit = {
    val pkValues: Seq[MultiColumnPrimaryKeyValue] = task.pkValues

    if (!Constants.dataCopyBatchSizes.contains(pkValues.size)) {
      throw new IllegalArgumentException(s"Unsupported data copy batch size: ${pkValues.size}")
    }

    val rowsToInsert: Vector[Row] =
      originDbAccess.getRowsFromPrimaryKeyValues(task.table, pkValues)

    if (!rowsToInsert.size.equals(pkValues.size)) {
      val message: String =
        s"Number of rows fetched did not equal number of primary keys. " +
          s"Rows fetched: ${rowsToInsert.size}. " +
          s"Primary keys to fetch by: ${pkValues.size}"
      throw new IllegalStateException(message)
    }

    targetDbAccess.insertRows(task.table, rowsToInsert)
  }
}

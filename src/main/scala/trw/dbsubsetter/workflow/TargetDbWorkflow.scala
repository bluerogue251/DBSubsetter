package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.{Constants, DbAccessFactory, PrimaryKeyValue, Row}


/*
 * TODO rename this to something more along the lines of "Data Copy Workflow" (as opposed to "Key Query Workflow"),
 *  since it involves both the origin and target dbs, not just the target db.
 */
final class TargetDbWorkflow(dbAccessFactory: DbAccessFactory) {

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

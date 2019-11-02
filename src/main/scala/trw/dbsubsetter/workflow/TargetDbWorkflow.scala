package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.{Constants, DbAccessFactory, PrimaryKeyValue, Row}
import trw.dbsubsetter.util.BatchingUtil


// TODO rename this to something more along the lines of "Data Copy Workflow" (as opposed to "Key Query Workflow")
final class TargetDbWorkflow(dbAccessFactory: DbAccessFactory) {

  private[this] val originDbAccess = dbAccessFactory.buildOriginDbAccess()

  private[this] val targetDbAccess = dbAccessFactory.buildTargetDbAccess()

  def process(request: DataCopyTask): Unit = {
    val pkValues: Seq[PrimaryKeyValue] = request.pkValues

    if (pkValues.nonEmpty) {
      val batchedPrimaryKeyValues: Seq[Seq[PrimaryKeyValue]] =
        BatchingUtil.batch(pkValues, Constants.dataCopyBatchSizes)

      batchedPrimaryKeyValues.foreach(primaryKeyValueBatch => {
        val rowsToInsert: Vector[Row] =
          originDbAccess.getRowsFromPrimaryKeyValues(request.table, primaryKeyValueBatch)

        targetDbAccess.insertRows(request.table, rowsToInsert)
      })
    }
  }
}

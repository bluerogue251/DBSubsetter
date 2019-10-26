package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{Constants, DbAccessFactory, PrimaryKeyValue, Row, SchemaInfo, Table}
import trw.dbsubsetter.util.BatchingUtil


// TODO rename this to something more along the lines of "Data Copy Workflow" (as opposed to "Key Query Workflow")
final class TargetDbWorkflow(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory) {

  private[this] val originDbAccess = dbAccessFactory.buildOriginDbAccess()

  private[this] val targetDbAccess = dbAccessFactory.buildTargetDbAccess()

  private[this] val pkOrdinalsByTable: Map[Table, Seq[Int]] =
    schemaInfo
      .pksByTableOrdered
      .map { case (table, pkColumns) =>
        table -> pkColumns.map(_.ordinalPosition)
      }

  def process(request: PksAdded): Unit = {
    val pkColumnOrdinals: Seq[Int] = pkOrdinalsByTable(request.table)

    /*
     * The fact that a row still needs parent tasks means this is the first time we've seen it. By extension, that
     * means it has not yet been added to the target db
     */
    val primaryKeyValues: Vector[PrimaryKeyValue] =
      request
        .rowsNeedingParentTasks
        .map(row => pkColumnOrdinals.map(row))
        .map(value => new PrimaryKeyValue(value))

    if (primaryKeyValues.nonEmpty) {
      val batchedPrimaryKeyValues: Seq[Seq[PrimaryKeyValue]] =
        BatchingUtil.batch(primaryKeyValues, Constants.dataCopyBatchSizes)

      batchedPrimaryKeyValues.foreach(primaryKeyValueBatch => {
        val rowsToInsert: Vector[Row] =
          originDbAccess.getRowsFromPrimaryKeyValues(request.table, primaryKeyValueBatch)

        targetDbAccess.insertRows(request.table, rowsToInsert)
      })
    }
  }
}

package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, PrimaryKeyValue, Row, SchemaInfo}


final class TargetDbWorkflow(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory) {
  private[this] val originDbAccess = dbAccessFactory.buildOriginDbAccess()
  private[this] val targetDbAccess = dbAccessFactory.buildTargetDbAccess()

  def process(request: PksAdded): Unit = {
    val pkColumnOrdinals: Seq[Int] =
      schemaInfo
        .pksByTableOrdered(request.table)
        .map(_.ordinalPosition)

    /*
     * The fact that a row still needs parent tasks means this is the first time we've seen it. By extension, that
     * means it has not yet been added to the target db
     */
    val primaryKeyValues: Vector[PrimaryKeyValue] =
      request
        .rowsNeedingParentTasks
        .map(row => pkColumnOrdinals.map(row))
        .map(value => new PrimaryKeyValue(value))

    val rowsToInsert: Vector[Row] =
      originDbAccess.getRowsFromPrimaryKeyValues(request.table, primaryKeyValues)

    targetDbAccess.insertRows(request.table, request.rowsNeedingParentTasks)
  }
}

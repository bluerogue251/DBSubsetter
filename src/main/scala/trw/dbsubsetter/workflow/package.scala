package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  sealed trait OriginDbRequest

  sealed trait ForeignKeyTask extends PkResult

  case class BaseQuery(table: Table, sql: SqlQuery, fetchChildren: Boolean) extends OriginDbRequest

  case class FetchParentTask(foreignKey: ForeignKey, value: Any) extends OriginDbRequest with ForeignKeyTask

  case class FetchChildrenTask(foreignKey: ForeignKey, value: Any) extends OriginDbRequest with ForeignKeyTask

  case class OriginDbResult(table: Table, rows: Vector[Row], viaTableOpt: Option[Table], fetchChildren: Boolean)

  case class TargetDbInsertResult(table: Table, numRowsInserted: Long)

  sealed trait PkResult

  case class PksAdded(table: Table, rowsNeedingParentTasks: Vector[Row], rowsNeedingChildTasks: Vector[Row], viaTableOpt: Option[Table]) extends PkResult

  case object DuplicateTask extends PkResult
}

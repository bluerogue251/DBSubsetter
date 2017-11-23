package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  case class FkTask(table: Table, fk: ForeignKey, fkValue: AnyRef, fetchChildren: Boolean) extends OriginDbRequest with PkResult

  sealed trait OriginDbRequest

  case class SqlStrQuery(table: Table, sql: SqlQuery, fetchChildren: Boolean) extends OriginDbRequest

  case class OriginDbResult(table: Table, rows: Vector[Row], fetchChildren: Boolean)

  case class TargetDbInsertResult(table: Table, numRowsInserted: Long)

  sealed trait PkResult

  case class PksAdded(table: Table, rowsNeedingParentTasks: Vector[Row], rowsNeedingChildTasks: Vector[Row]) extends PkResult

  case object DuplicateTask extends PkResult
}

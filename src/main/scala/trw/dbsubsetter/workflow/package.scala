package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {
  case class FkTask(table: Table, fk: ForeignKey, fkValue: Vector[AnyRef], fetchChildren: Boolean) extends OriginDbRequest with PkRequest with PkResult

  sealed trait OriginDbRequest

  case class SqlStrQuery(table: Table, sql: SqlQuery) extends OriginDbRequest

  case class OriginDbResult(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkRequest

  case class TargetDbInsertRequest(table: Table, rows: Vector[Row])

  case class TargetDbInsertResult(table: Table, rowsCopied: Long)

  sealed trait PkRequest

  sealed trait PkResult

  case class PksAdded(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkResult
}

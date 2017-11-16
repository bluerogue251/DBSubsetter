package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  case class FkTask(table: Table, fk: ForeignKey, fkValue: AnyRef, fetchChildren: Boolean) extends OriginDbRequest with PkRequest with PkResult

  sealed trait OriginDbRequest

  case class SqlStrQuery(table: Table, sql: SqlQuery) extends OriginDbRequest

  case class OriginDbResult(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkRequest

  case class TargetDbInsertResult(table: Table, rowsCopied: Long)

  sealed trait PkRequest {
    def table: Table
  }

  sealed trait PkResult

  case class PksAdded(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkResult
}

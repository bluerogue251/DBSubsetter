package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  // Fk Task Domain Object
  case class FkTask(table: Table, fk: ForeignKey, fkValue: Vector[AnyRef], fetchChildren: Boolean) extends OriginDbRequest with PkRequest with PkResult

  // Origin Db Domain Objects
  sealed trait OriginDbRequest

  case class SqlStrQuery(table: Table, sql: SqlQuery) extends OriginDbRequest

  case class OriginDbResult(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkRequest

  // Target DB Domain Objects
  case class DbInsertRequest(table: Table, rows: Vector[Row])

  case class DbInsertResult(table: Table, rowsCopied: Long)

  // PK Request Domain Objects
  sealed trait PkRequest

  // PK Result Domain Objects
  sealed trait PkResult

  case class PksAdded(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkResult
}

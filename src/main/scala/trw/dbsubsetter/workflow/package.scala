package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  // Fk Task Domain Object
  case class FkTask(table: Table, fk: ForeignKey, values: Vector[AnyRef], fetchChildren: Boolean)

  // Origin Db Domain Objects
  sealed trait OriginDbRequest

  case class FkQuery(task: FkTask) extends OriginDbRequest

  case class SqlStrQuery(table: Table, sql: SqlQuery) extends OriginDbRequest

  case class OriginDbResult(table: Table, rows: Vector[Row], fetchChildren: Boolean)

  // Target DB Domain Objects
  case class DbInsertRequest(table: Table, rows: Vector[Row])

  case class DbInsertResult(table: Table, rowsCopied: Long)

  // PK Request Domain Objects
  sealed trait PkRequest

  case class PkExistRequest(task: FkTask) extends PkRequest

  case class PkAddRequest(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkRequest


  // PK Result Domain Objects
  sealed trait PkResult

  case class PksAdded(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkResult

  case class PkMissing(fkTask: FkTask) extends PkResult

}

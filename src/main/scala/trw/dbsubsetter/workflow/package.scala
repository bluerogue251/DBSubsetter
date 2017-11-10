package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  // Fk Task Domain Object
  case class FkTask(table: Table, fk: ForeignKey, values: Vector[AnyRef], fetchChildren: Boolean)

  // DbRequest Domain Objects
  sealed trait DbRequest

  case class FkQuery(task: FkTask) extends DbRequest

  case class SqlStrQuery(table: Table, cols: Seq[Column], sql: SqlQuery) extends DbRequest

  case class DbCopy(pk: PrimaryKey, pkValues: Set[Vector[AnyRef]]) extends DbRequest

  // DbResult Domain Object
  sealed trait DbResult

  case class DbFetchResult(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends DbResult

  case class DbCopyResult(table: Table, rowsCopied: Long) extends DbResult

  // PK Request Domain Objects
  sealed trait PkRequest

  case class PkExistRequest(task: FkTask) extends PkRequest

  case class PkAddRequest(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkRequest


  // PK Result Domain Objects
  sealed trait PkResult

  case class PksAdded(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkResult

  case class PkMissing(fkTask: FkTask) extends PkResult

}

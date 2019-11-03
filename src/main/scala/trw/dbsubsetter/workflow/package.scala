package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  sealed trait OriginDbRequest

  case class BaseQuery(table: Table, sql: SqlQuery, fetchChildren: Boolean) extends OriginDbRequest

  sealed trait ForeignKeyTask extends OriginDbRequest
  case class FetchParentTask(fk: ForeignKey, fkValueFromChild: ForeignKeyValue) extends ForeignKeyTask
  case class FetchChildrenTask(fk: ForeignKey, fkValueFromParent: ForeignKeyValue) extends ForeignKeyTask

  case class OriginDbResult(table: Table, rows: Vector[Keys], viaTableOpt: Option[Table], fetchChildren: Boolean)

  case class PksAdded(table: Table, rowsNeedingParentTasks: Vector[Keys], rowsNeedingChildTasks: Vector[Keys], viaTableOpt: Option[Table])

  class DataCopyTask(val table: Table, val pkValues: Seq[PrimaryKeyValue])

  sealed trait PkQueryResult
  case object AlreadySeen extends PkQueryResult
  case class NotAlreadySeen(task: FetchParentTask) extends PkQueryResult
}

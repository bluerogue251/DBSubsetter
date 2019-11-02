package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  sealed trait OriginDbRequest

  case class BaseQuery(table: Table, sql: SqlQuery, fetchChildren: Boolean) extends OriginDbRequest

  sealed trait ForeignKeyTask extends OriginDbRequest
  case class FetchParentTask(parentTable: Table, fk: ForeignKey, fkValueFromChild: ForeignKeyValue) extends ForeignKeyTask
  case class FetchChildrenTask(childTable: Table, viaParentTable: Table, fk: ForeignKey, fkValueFromParent: ForeignKeyValue) extends ForeignKeyTask

  case class OriginDbResult(table: Table, rows: Vector[Row], viaTableOpt: Option[Table], fetchChildren: Boolean)

  case class PksAdded(table: Table, rowsNeedingParentTasks: Vector[Row], rowsNeedingChildTasks: Vector[Row], viaTableOpt: Option[Table])

  class DataCopyTask(val table: Table, val pkValues: Seq[PrimaryKeyValue])

  sealed trait PkQueryResult
  case object AlreadySeen extends PkQueryResult
  case class NotAlreadySeen(task: FetchParentTask) extends PkQueryResult

  case class NewTasks(taskInfo: Map[(ForeignKey, Boolean), Seq[ForeignKeyValue]])
  val EmptyNewTasks = NewTasks(Map.empty)
}

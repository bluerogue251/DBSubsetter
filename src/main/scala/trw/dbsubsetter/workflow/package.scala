package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  sealed trait OriginDbRequest

  case class BaseQuery(table: Table, sql: SqlQuery, fetchChildren: Boolean) extends OriginDbRequest

  sealed trait ForeignKeyTask extends OriginDbRequest
  case class FetchParentTask(parentTable: Table, fk: ForeignKey, fkValueFromChild: Any) extends ForeignKeyTask
  case class FetchChildrenTask(childTable: Table, viaParentTable: Table, fk: ForeignKey, fkValueFromParent: Any) extends ForeignKeyTask

  case class OriginDbResult(table: Table, rows: Vector[KeyData], viaTableOpt: Option[Table], fetchChildren: Boolean)

  case class PksAdded(table: Table, rowsNeedingParentTasks: Vector[KeyData], rowsNeedingChildTasks: Vector[KeyData], viaTableOpt: Option[Table])

  class DataCopyTask(val table: Table, val pkValues: Seq[PrimaryKeyValue])

  sealed trait PkQueryResult
  case object AlreadySeen extends PkQueryResult
  case class NotAlreadySeen(task: FetchParentTask) extends PkQueryResult

  case class NewTasks(taskInfo: Map[(ForeignKey, Boolean), Array[Any]])
  val EmptyNewTasks = NewTasks(Map.empty)
}

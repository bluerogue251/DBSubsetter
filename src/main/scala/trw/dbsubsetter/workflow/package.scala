package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  sealed trait OriginDbRequest
  case class FkTask(table: Table, fk: ForeignKey, fkValue: Any, fetchChildren: Boolean) extends OriginDbRequest
  case class BaseQuery(table: Table, sql: SqlQuery, fetchChildren: Boolean) extends OriginDbRequest

  case class OriginDbResult(table: Table, rows: Vector[Row], viaTableOpt: Option[Table], fetchChildren: Boolean)

  case class TargetDbInsertResult(table: Table, numRowsInserted: Long)

  case class PksAdded(table: Table, rowsNeedingParentTasks: Vector[Row], rowsNeedingChildTasks: Vector[Row], viaTableOpt: Option[Table])

  sealed trait PkQueryResult
  case object AlreadySeen extends PkQueryResult
  case class NotAlreadySeen(task: FkTask) extends PkQueryResult

  case class NewTasks(taskInfo: Map[(ForeignKey, Boolean), Array[Any]])
  val EmptyNewTasks = NewTasks(Map.empty)
}

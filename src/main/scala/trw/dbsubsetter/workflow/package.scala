package trw.dbsubsetter

import trw.dbsubsetter.db._

package object workflow {

  // Fk Task Domain Object
  case class FkTask(table: Table, fk: ForeignKey, values: Vector[AnyRef], fetchChildren: Boolean)

  // DbRequest Domain Objects
  sealed trait DbRequest {
  }

  sealed trait DbFetch extends DbRequest {
    def table: Table
  }

  case class FkQuery(task: FkTask) extends DbFetch {
    override def table: Table = task.table
  }

  case class SqlStrQuery(table: Table, cols: Seq[Column], sql: SqlQuery) extends DbFetch

  case class DbCopy(pk: PrimaryKey, pkValues: Set[Vector[AnyRef]]) extends DbRequest

  // DbResult Domain Object
  case class DbResult(table: Table, rows: Vector[Row], fetchChildren: Boolean)

  // PK Request Domain Objects
  sealed trait PkRequest

  case class PkExistRequest(task: FkTask) extends PkRequest

  case class PkAddRequest(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkRequest


  // PK Result Domain Objects
  sealed trait PkResult

  case class PksAdded(table: Table, rows: Vector[Row], fetchChildren: Boolean) extends PkResult

  case class PkMissing(fkTask: FkTask) extends PkResult

}

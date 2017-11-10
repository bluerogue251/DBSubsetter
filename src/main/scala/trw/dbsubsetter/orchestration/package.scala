package trw.dbsubsetter

package object orchestration {

  // Fk Task Domain Object
  case class FkTask(table: Table, fk: ForeignKey, values: Vector[AnyRef], fetchChildren: Boolean)

  // DbRequest Domain Objects
  sealed trait DbRequest

  sealed trait DbFetch extends DbRequest {
    def table: Table
  }

  case class FkQuery(task: FkTask) extends DbFetch {
    override def table: Table = task.table
  }

  case class SqlStrQuery(table: Table, cols: Seq[Column], sql: SqlQuery) extends DbFetch

  case class DbCopy(pk: PrimaryKey, pkValues: Set[Vector[AnyRef]]) extends DbRequest

  // DbResult Domain Object
  case class DbResult(request: DbRequest, rows: Vector[Row])

  // PK Request Domain Objects
  sealed trait PkRequest

  case class PkExists(task: FkTask) extends PkRequest

  case class PkAdd(table: Table, rows: Vector[Row]) extends PkRequest
}

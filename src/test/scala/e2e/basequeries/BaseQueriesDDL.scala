package e2e.basequeries

trait BaseQueriesDDL {
  protected val profile: slick.jdbc.JdbcProfile

  import profile.api._

  lazy val schema: profile.SchemaDescription = BaseTable.schema ++ ChildTable.schema

  /**
    * base_table
    */
  case class BaseTableRow(id: Int)

  class BaseTable(_tableTag: Tag) extends profile.api.Table[BaseTableRow](_tableTag, "base_table") {
    def * = id <> (BaseTableRow, BaseTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  }

  lazy val BaseTable = new TableQuery(tag => new BaseTable(tag))

  /**
    * child_table
    */
  case class ChildTableRow(id: Int, baseTableId: Int)

  class ChildTable(_tableTag: Tag) extends profile.api.Table[ChildTableRow](_tableTag, "child_table") {
    def * = (id, baseTableId) <> (ChildTableRow.tupled, ChildTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val baseTableId: Rep[Int] = column[Int]("base_table_id")
    lazy val baseTableFk = foreignKey("child_table_base_table_id_fkey", baseTableId, BaseTable)(_.id)
  }

  lazy val ChildTable = new TableQuery(tag => new ChildTable(tag))
}
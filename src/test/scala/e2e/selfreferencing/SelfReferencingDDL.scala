package e2e.selfreferencing

trait SelfReferencingDDL {
  val profile: slick.jdbc.JdbcProfile

  import profile.api._
  import slick.model.ForeignKeyAction

  lazy val schema: profile.SchemaDescription = SelfReferencingTable.schema

  /**
    * self_referencing_table
    */
  case class SelfReferencingTableRow(id: Int, parentId: Int)

  class SelfReferencingTable(_tableTag: Tag) extends profile.api.Table[SelfReferencingTableRow](_tableTag, "self_referencing_table") {
    def * = (id, parentId) <> (SelfReferencingTableRow.tupled, SelfReferencingTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val parentId: Rep[Int] = column[Int]("parent_id")
    lazy val selfReferencingTableFk = foreignKey("self_referencing_table_parent_id_fkey", parentId, SelfReferencingTable)(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  lazy val SelfReferencingTable = new TableQuery(tag => new SelfReferencingTable(tag))
}

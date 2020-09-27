package e2e.validation

import slick.jdbc.JdbcProfile

class ValidationDDL(val profile: JdbcProfile) {

  import profile.api._

  lazy val schema: profile.SchemaDescription = SelfReferencingTable.schema

  /**
    * self_referencing_table
    */
  case class SelfReferencingTableRow(id: Int, parentId: Option[Int])

  class SelfReferencingTable(_tableTag: Tag)
      extends profile.api.Table[SelfReferencingTableRow](_tableTag, "self_referencing_table") {
    def * = (id, parentId) <> (SelfReferencingTableRow.tupled, SelfReferencingTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val parentId: Rep[Option[Int]] = column[Option[Int]]("parent_id")
    lazy val selfReferencingTableFk =
      foreignKey("self_referencing_table_parent_id_fkey", parentId, SelfReferencingTable)(r => Rep.Some(r.id))
  }

  lazy val SelfReferencingTable = new TableQuery(tag => new SelfReferencingTable(tag))
}

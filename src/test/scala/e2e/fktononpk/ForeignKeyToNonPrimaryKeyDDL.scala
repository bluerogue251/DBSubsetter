package e2e.fktononpk
import slick.jdbc.JdbcProfile

class ForeignKeyToNonPrimaryKeyDDL(val profile: JdbcProfile) {
  import profile.api._

  lazy val schema: profile.SchemaDescription = Array(ReferencedTable.schema, ReferencingTable.schema).reduceLeft(_ ++ _)

  /**
    * referenced_table
    */
  case class ReferencedTableRow(id: Int, businessKey: String)

  class ReferencedTable(_tableTag: Tag) extends profile.api.Table[ReferencedTableRow](_tableTag, "referenced_table") {
    def * = (id, businessKey) <> (ReferencedTableRow.tupled, ReferencedTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val businessKey: Rep[String] = column[String]("businessKey", O.Length(40, varying = true), O.Unique)
  }

  lazy val ReferencedTable = new TableQuery(tag => new ReferencedTable(tag))

  /**
    * referencing_table
    */
  case class ReferencingTableRow(id: Int, referencedTableBusinessKey: String)

  class ReferencingTable(_tableTag: Tag) extends profile.api.Table[ReferencingTableRow](_tableTag, "referencing_table") {
    def * = (id, referencedTableBusinessKey) <> (ReferencingTableRow.tupled, ReferencingTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val referencedTableBusinessKey: Rep[String] = column[String]("referenced_table_business_key", O.Length(40, varying = true))

    lazy val fk = foreignKey("fkey", referencedTableBusinessKey, ReferencedTable)(_.businessKey)
    val index1 = index("referencing_table_referenced_table_business_key_idx", referencedTableBusinessKey)
  }

  lazy val ReferencingTable = new TableQuery(tag => new ReferencingTable(tag))
}

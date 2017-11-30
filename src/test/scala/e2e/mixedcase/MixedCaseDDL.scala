package e2e.mixedcase

trait MixedCaseDDL {
  val profile: slick.jdbc.JdbcProfile

  import profile.api._
  import slick.model.ForeignKeyAction

  lazy val schema: profile.SchemaDescription = MixedCaseTable1.schema ++ MixedCaseTable2.schema

  /**
    * mixed_case_table_1
    */
  case class MixedCaseTable1Row(id: Int)

  class MixedCaseTable1(_tableTag: Tag) extends profile.api.Table[MixedCaseTable1Row](_tableTag, "mixed_CASE_table_1") {
    def * = id <> (MixedCaseTable1Row, MixedCaseTable1Row.unapply)

    val id: Rep[Int] = column[Int]("ID", O.PrimaryKey)
  }

  lazy val MixedCaseTable1 = new TableQuery(tag => new MixedCaseTable1(tag))

  /**
    * mixed_case_table_2
    */
  case class MixedCaseTable2Row(id: Int, mixedCaseTable1Id: Int)

  class MixedCaseTable2(_tableTag: Tag) extends profile.api.Table[MixedCaseTable2Row](_tableTag, "MIXED_CASE_TABLE_2") {
    def * = (id, mixedCaseTable1Id) <> (MixedCaseTable2Row.tupled, MixedCaseTable2Row.unapply)

    val id: Rep[Int] = column[Int]("iD", O.PrimaryKey)
    val mixedCaseTable1Id: Rep[Int] = column[Int]("mixed_case_TABLE_1_id")
    lazy val selfReferencingTableFk = foreignKey("fkey", mixedCaseTable1Id, MixedCaseTable1)(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  lazy val MixedCaseTable2 = new TableQuery(tag => new MixedCaseTable2(tag))
}

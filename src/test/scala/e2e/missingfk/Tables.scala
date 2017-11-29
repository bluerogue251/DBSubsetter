package e2e.missingfk


trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction

  lazy val schema: profile.SchemaDescription = Array(Table1.schema, Table2.schema, Table3.schema, Table4.schema, Table5.schema, TableA.schema, TableB.schema, TableC.schema, TableD.schema).reduceLeft(_ ++ _)

  def createSchema = schema.create

  case class Table1Row(id: Int)
  class Table1(_tableTag: Tag) extends profile.api.Table[Table1Row](_tableTag, "table_1") {
    def * = id <> (Table1Row, Table1Row.unapply)
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  }
  lazy val Table1 = new TableQuery(tag => new Table1(tag))

  case class Table2Row(id: Int, table1Id: Int)
  class Table2(_tableTag: Tag) extends profile.api.Table[Table2Row](_tableTag, "table_2") {
    def * = (id, table1Id) <> (Table2Row.tupled, Table2Row.unapply)
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val table1Id: Rep[Int] = column[Int]("table_1_id")
    val index1 = index("table_2_table_1_id_idx", table1Id)
  }
  lazy val Table2 = new TableQuery(tag => new Table2(tag))

  case class Table3Row(id: Int)

  class Table3(_tableTag: Tag) extends profile.api.Table[Table3Row](_tableTag, "table_3") {
    def * = id <> (Table3Row, Table3Row.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  }

  lazy val Table3 = new TableQuery(tag => new Table3(tag))

  case class Table4Row(table1Id: Int, table3Id: Int)

  class Table4(_tableTag: Tag) extends profile.api.Table[Table4Row](_tableTag, "table_4") {
    def * = (table1Id, table3Id) <> (Table4Row.tupled, Table4Row.unapply)

    val table1Id: Rep[Int] = column[Int]("table_1_id")
    val table3Id: Rep[Int] = column[Int]("table_3_id")
    lazy val table1Fk = foreignKey("table_4_table_1_id_fkey", table1Id, Table1)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val table3Fk = foreignKey("table_4_table_3_id_fkey", table3Id, Table3)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    val index1 = index("table_4_table_1_id_table_3_id_key", (table1Id, table3Id), unique = true)
  }

  lazy val Table4 = new TableQuery(tag => new Table4(tag))

  case class Table5Row(id: Int, table4Table1Id: Int, table4Table3Id: Int)

  class Table5(_tableTag: Tag) extends profile.api.Table[Table5Row](_tableTag, "table_5") {
    def * = (id, table4Table1Id, table4Table3Id) <> (Table5Row.tupled, Table5Row.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val table4Table1Id: Rep[Int] = column[Int]("table_4_table_1_id")
    val table4Table3Id: Rep[Int] = column[Int]("table_4_table_3_id")
    lazy val table4Fk = foreignKey("table_5_table_4_table_1_id_fkey", (table4Table1Id, table4Table3Id), Table4)(r => (r.table1Id, r.table3Id), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  lazy val Table5 = new TableQuery(tag => new Table5(tag))

  case class TableARow(id: Int, pointsToTableName: String, pointsToTableId: Int)

  class TableA(_tableTag: Tag) extends profile.api.Table[TableARow](_tableTag, "table_a") {
    def * = (id, pointsToTableName, pointsToTableId) <> (TableARow.tupled, TableARow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val pointsToTableName: Rep[String] = column[String]("points_to_table_name", O.Length(255, varying = true))
    val pointsToTableId: Rep[Int] = column[Int]("points_to_table_id")

    val index1 = index("table_a_points_to_table_id_idx", pointsToTableId)
    val index2 = index("table_a_points_to_table_name_idx", pointsToTableName)
  }

  lazy val TableA = new TableQuery(tag => new TableA(tag))

  case class TableBRow(id: Int)

  class TableB(_tableTag: Tag) extends profile.api.Table[TableBRow](_tableTag, "table_b") {
    def * = id <> (TableBRow, TableBRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  }

  lazy val TableB = new TableQuery(tag => new TableB(tag))


  case class TableCRow(id: Int)

  class TableC(_tableTag: Tag) extends profile.api.Table[TableCRow](_tableTag, "table_c") {
    def * = id <> (TableCRow, TableCRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  }

  lazy val TableC = new TableQuery(tag => new TableC(tag))

  case class TableDRow(id: Int)

  class TableD(_tableTag: Tag) extends profile.api.Table[TableDRow](_tableTag, "table_d") {
    def * = id <> (TableDRow, TableDRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  }

  lazy val TableD = new TableQuery(tag => new TableD(tag))
}

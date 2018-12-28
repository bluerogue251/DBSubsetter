package e2e.autoincrementingpk

trait AutoIncrementingPkDDL {
  protected val profile: slick.jdbc.JdbcProfile

  import profile.api._

  lazy val schema: profile.SchemaDescription = AutoincrementingPkTable.schema ++ OtherAutoincrementingPkTable.schema

  /**
    * autoincrementing_pk_table
    */
  case class AutoincrementingPkTableRow(id: Int, note: String)

  class AutoincrementingPkTable(_tableTag: Tag) extends profile.api.Table[AutoincrementingPkTableRow](_tableTag, "autoincrementing_pk_table") {
    def * = (id, note) <> (AutoincrementingPkTableRow.tupled, AutoincrementingPkTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val note: Rep[String] = column[String]("note")
  }

  lazy val AutoincrementingPkTable = new TableQuery(tag => new AutoincrementingPkTable(tag))

  /**
    * other_autoincrementing_pk_table
    */
  case class OtherAutoincrementingPkTableRow(id: Int, note: String)

  class OtherAutoincrementingPkTable(_tableTag: Tag) extends profile.api.Table[OtherAutoincrementingPkTableRow](_tableTag, "other_autoincrementing_pk_table") {
    def * = (id, note) <> (OtherAutoincrementingPkTableRow.tupled, OtherAutoincrementingPkTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val note: Rep[String] = column[String]("note")
  }

  lazy val OtherAutoincrementingPkTable = new TableQuery(tag => new OtherAutoincrementingPkTable(tag))
}

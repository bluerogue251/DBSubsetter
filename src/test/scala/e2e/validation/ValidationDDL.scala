package e2e.validation

import slick.jdbc.JdbcProfile

class ValidationDDL(val profile: JdbcProfile) {

  import profile.api._

  lazy val schema: profile.SchemaDescription = ValidationTableQuery.schema

  case class ValidationRow(id: Int, contents: String)

  class ValidationTable(_tableTag: Tag) extends profile.api.Table[ValidationRow](_tableTag, "validation") {
    def * = (id, contents) <> (ValidationRow.tupled, ValidationRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val contents: Rep[String] = column[String]("contents")
  }

  lazy val ValidationTableQuery = new TableQuery(tag => new ValidationTable(tag))
}

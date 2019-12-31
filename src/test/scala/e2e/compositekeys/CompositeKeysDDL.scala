package e2e.compositekeys

import slick.jdbc.JdbcProfile
import slick.lifted.PrimaryKey

class CompositeKeysDDL(val profile: JdbcProfile) {
  import profile.api._

  lazy val schema: profile.SchemaDescription = ParentTable.schema ++ ChildTable.schema

  /**
    * parents
    */
  case class ParentTableRow(idOne: Int, idTwo: Int)

  class ParentTable(_tableTag: Tag) extends profile.api.Table[ParentTableRow](_tableTag, "parents") {
    def * = (idOne, idTwo) <> (ParentTableRow.tupled, ParentTableRow.unapply)

    val idOne: Rep[Int] = column[Int]("id_one")
    val idTwo: Rep[Int] = column[Int]("id_two")
    val pk: PrimaryKey = primaryKey("composite_pk", (idOne, idTwo))
  }

  lazy val ParentTable = new TableQuery(tag => new ParentTable(tag))

  /**
    * children
    */
  case class ChildTableRow(id: Int, parentIdOne: Int, parentIdTwo: Int)

  class ChildTable(_tableTag: Tag) extends profile.api.Table[ChildTableRow](_tableTag, "children") {
    def * = (id, parentIdOne, parentIdTwo) <> (ChildTableRow.tupled, ChildTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val parentIdOne: Rep[Int] = column[Int]("parent_id_one")
    val parentIdTwo: Rep[Int] = column[Int]("parent_id_two")
    lazy val fk = foreignKey("foreign_key", (parentIdOne, parentIdTwo), ParentTable)(t => (t.idOne, t.idTwo))
  }

  lazy val ChildTable = new TableQuery(tag => new ChildTable(tag))
}

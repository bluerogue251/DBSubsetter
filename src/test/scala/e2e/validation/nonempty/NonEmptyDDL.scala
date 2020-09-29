package e2e.validation.nonempty

import slick.jdbc.JdbcProfile

class NonEmptyDDL(val profile: JdbcProfile) {
  import profile.api._

  lazy val schema: profile.SchemaDescription = Foo.schema ++ Bar.schema ++ Baz.schema

  /**
    * foo
    */
  case class FooRow(id: Int)

  class FooTable(tag: Tag) extends profile.api.Table[FooRow](tag, Some("valid_schema"), "foo") {
    def * = id <> (FooRow, FooRow.unapply)

    val id: Rep[Int] = column[Int]("ID", O.PrimaryKey)
  }

  lazy val Foo = new TableQuery(tag => new FooTable(tag))

  /**
    * bar
    */
  case class BarRow(id: Int, mixedCaseTable1Id: Int)

  class BarTable(tag: Tag) extends profile.api.Table[BarRow](tag, Some("valid_schema"), "bar") {
    def * = (id, fooId) <> (BarRow.tupled, BarRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val fooId: Rep[Int] = column[Int]("foo_id")
    lazy val fooFk = foreignKey("fkey", fooId, Foo)(_.id)
  }

  lazy val Bar = new TableQuery(tag => new BarTable(tag))

  /**
    * baz
    */
  case class BazRow(id: Int, mixedCaseTable1Id: Int)

  class BazTable(tag: Tag) extends profile.api.Table[BazRow](tag, Some("valid_schema"), "baz") {
    def * = (id, barId) <> (BazRow.tupled, BazRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val barId: Rep[Int] = column[Int]("bar_id")
    lazy val barFk = foreignKey("fkey", barId, Bar)(_.id)
  }

  lazy val Baz = new TableQuery(tag => new BazTable(tag))
}

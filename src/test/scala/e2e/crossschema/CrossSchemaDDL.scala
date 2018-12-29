package e2e.crossschema

import slick.jdbc.JdbcProfile

class CrossSchemaDDL(val profile: JdbcProfile) {
  import profile.api._

  lazy val schema: profile.SchemaDescription = Schema1Table.schema ++ Schema2Table.schema ++ Schema3Table.schema

  /**
    * schema_1_table
    */
  case class Schema1TableRow(id: Int)

  class Schema1Table(_tableTag: Tag) extends profile.api.Table[Schema1TableRow](_tableTag, Some("schema_1"), "schema_1_table") {
    def * = id <> (Schema1TableRow, Schema1TableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  }

  lazy val Schema1Table = new TableQuery(tag => new Schema1Table(tag))

  /**
    * schema_TWO_table
    */
  case class Schema2TableRow(id: Int, schema1TableId: Int)

  class Schema2Table(_tableTag: Tag) extends profile.api.Table[Schema2TableRow](_tableTag, Some("schema_2"), "schema_2_table") {
    def * = (id, schema1TableId) <> (Schema2TableRow.tupled, Schema2TableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val schema1TableId: Rep[Int] = column[Int]("schema_1_table_id")
    lazy val fk = foreignKey("fkey", schema1TableId, Schema1Table)(_.id)
  }

  lazy val Schema2Table = new TableQuery(tag => new Schema2Table(tag))

  /**
    * schema_3_table
    */
  case class Schema3TableRow(id: Int, schema2TableId: Int)

  class Schema3Table(_tableTag: Tag) extends profile.api.Table[Schema3TableRow](_tableTag, Some("schema_3"), "schema_3_table") {
    def * = (id, schema2TableId) <> (Schema3TableRow.tupled, Schema3TableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val schema2TableId: Rep[Int] = column[Int]("schema_2_table_id")
    lazy val fk = foreignKey("fkey", schema2TableId, Schema2Table)(_.id)
  }

  lazy val Schema3Table = new TableQuery(tag => new Schema3Table(tag))
}

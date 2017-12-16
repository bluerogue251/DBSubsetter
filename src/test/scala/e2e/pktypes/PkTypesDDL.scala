package e2e.pktypes

import java.util.UUID

trait PkTypesDDL {
  val profile: slick.jdbc.JdbcProfile

  import profile.api._

  lazy val schema: profile.SchemaDescription = Seq(BytePkTable, ShortPkTable, IntPkTable, LongPkTable, UUIDPkTable, StringPkTable, ReferencingTable).map(_.schema).reduce(_ ++ _)

  case class BytePkTableRow(id: Byte)

  class BytePkTable(_tableTag: Tag) extends profile.api.Table[BytePkTableRow](_tableTag, "byte_pks") {
    def * = id <> (BytePkTableRow, BytePkTableRow.unapply)

    val id: Rep[Byte] = column[Byte]("id", O.PrimaryKey)
  }

  lazy val BytePkTable = new TableQuery(tag => new BytePkTable(tag))

  case class ShortPkTableRow(id: Short)

  class ShortPkTable(_tableTag: Tag) extends profile.api.Table[ShortPkTableRow](_tableTag, "short_pks") {
    def * = id <> (ShortPkTableRow, ShortPkTableRow.unapply)

    val id: Rep[Short] = column[Short]("id", O.PrimaryKey)
  }

  lazy val ShortPkTable = new TableQuery(tag => new ShortPkTable(tag))

  case class IntPkTableRow(id: Int)

  class IntPkTable(_tableTag: Tag) extends profile.api.Table[IntPkTableRow](_tableTag, "int_pks") {
    def * = id <> (IntPkTableRow, IntPkTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  }

  lazy val IntPkTable = new TableQuery(tag => new IntPkTable(tag))

  case class LongPkTableRow(id: Long)

  class LongPkTable(_tableTag: Tag) extends profile.api.Table[LongPkTableRow](_tableTag, "long_pks") {
    def * = id <> (LongPkTableRow, LongPkTableRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
  }

  lazy val LongPkTable = new TableQuery(tag => new LongPkTable(tag))

  case class UUIDPkTableRow(id: UUID)

  class UUIDPkTable(_tableTag: Tag) extends profile.api.Table[UUIDPkTableRow](_tableTag, "uuid_pks") {
    def * = id <> (UUIDPkTableRow, UUIDPkTableRow.unapply)

    val id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  }

  lazy val UUIDPkTable = new TableQuery(tag => new UUIDPkTable(tag))

  case class StringPkTableRow(id: String)

  class StringPkTable(_tableTag: Tag) extends profile.api.Table[StringPkTableRow](_tableTag, "string_pks") {
    def * = id <> (StringPkTableRow, StringPkTableRow.unapply)

    val id: Rep[String] = column[String]("id", O.PrimaryKey)
  }

  lazy val StringPkTable = new TableQuery(tag => new StringPkTable(tag))

  case class ReferencingTableRow(id: Int, byteId: Option[Byte], shortId: Option[Short], intId: Option[Int], longId: Option[Long], uuidId: Option[UUID], stringId: Option[String])

  class ReferencingTable(_tableTag: Tag) extends profile.api.Table[ReferencingTableRow](_tableTag, "referencing_table") {
    def * = (id, byteId, shortId, intId, longId, uuidId, stringId) <> (ReferencingTableRow.tupled, ReferencingTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val byteId: Rep[Option[Byte]] = column[Option[Byte]]("byte_id")
    val shortId: Rep[Option[Short]] = column[Option[Short]]("short_id")
    val intId: Rep[Option[Int]] = column[Option[Int]]("int_id")
    val longId: Rep[Option[Long]] = column[Option[Long]]("long_id")
    val uuidId: Rep[Option[UUID]] = column[Option[UUID]]("uuid_id")
    val stringId: Rep[Option[String]] = column[Option[String]]("string_id")

    lazy val byteIdFk = foreignKey("byte_id_fkey", byteId, BytePkTable)(_.id)
    lazy val shortIdFk = foreignKey("short_id_fkey", shortId, ShortPkTable)(_.id)
    lazy val intIdFk = foreignKey("int_id_fkey", intId, IntPkTable)(_.id)
    lazy val longIdFk = foreignKey("long_id_fkey", longId, LongPkTable)(_.id)
    lazy val uuidIdFk = foreignKey("uuid_id_fkey", uuidId, UUIDPkTable)(_.id)
    lazy val stringIdFk = foreignKey("string_id_fkey", stringId, StringPkTable)(_.id)
  }

  lazy val ReferencingTable = new TableQuery(tag => new ReferencingTable(tag))
}

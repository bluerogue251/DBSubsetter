package e2e.pktypes

import java.util.UUID

trait PkTypesDDL {
  val profile: slick.jdbc.JdbcProfile

  import profile.api._

  lazy val schema: profile.SchemaDescription = Seq(BytePkTable, ShortPkTable, IntPkTable, LongPkTable, UUIDPkTable, Char10PkTable, Varchar10PkTable, ReferencingTable).map(_.schema).reduce(_ ++ _)

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

  case class Char10PkTableRow(id: String)

  class Char10PkTable(_tableTag: Tag) extends profile.api.Table[Char10PkTableRow](_tableTag, "char_10_pks") {
    def * = id <> (Char10PkTableRow, Char10PkTableRow.unapply)

    val id: Rep[String] = column[String]("id", O.PrimaryKey, O.Length(10, varying = false))
  }

  lazy val Char10PkTable = new TableQuery(tag => new Char10PkTable(tag))

  case class Varchar10PkTableRow(id: String)

  class Varchar10PkTable(_tableTag: Tag) extends profile.api.Table[Varchar10PkTableRow](_tableTag, "varchar_10_pks") {
    def * = id <> (Varchar10PkTableRow, Varchar10PkTableRow.unapply)

    val id: Rep[String] = column[String]("id", O.PrimaryKey, O.Length(10, varying = true))
  }

  lazy val Varchar10PkTable = new TableQuery(tag => new Varchar10PkTable(tag))

  case class ReferencingTableRow(id: Int, byteId: Option[Byte], shortId: Option[Short], intId: Option[Int], longId: Option[Long], uuidId: Option[UUID], char10Id: Option[String], varchar10Id: Option[String])

  class ReferencingTable(_tableTag: Tag) extends profile.api.Table[ReferencingTableRow](_tableTag, "referencing_table") {
    def * = (id, byteId, shortId, intId, longId, uuidId, char10Id, varchar10Id) <> (ReferencingTableRow.tupled, ReferencingTableRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val byteId: Rep[Option[Byte]] = column[Option[Byte]]("byte_id")
    val shortId: Rep[Option[Short]] = column[Option[Short]]("short_id")
    val intId: Rep[Option[Int]] = column[Option[Int]]("int_id")
    val longId: Rep[Option[Long]] = column[Option[Long]]("long_id")
    val uuidId: Rep[Option[UUID]] = column[Option[UUID]]("uuid_id")
    val char10Id: Rep[Option[String]] = column[Option[String]]("char_10_id", O.Length(10, varying = false))
    val varchar10Id: Rep[Option[String]] = column[Option[String]]("varchar_10_id", O.Length(10, varying = true))

    lazy val byteIdFk = foreignKey("byte_id_fkey", byteId, BytePkTable)(_.id)
    lazy val shortIdFk = foreignKey("short_id_fkey", shortId, ShortPkTable)(_.id)
    lazy val intIdFk = foreignKey("int_id_fkey", intId, IntPkTable)(_.id)
    lazy val longIdFk = foreignKey("long_id_fkey", longId, LongPkTable)(_.id)
    lazy val uuidIdFk = foreignKey("uuid_id_fkey", uuidId, UUIDPkTable)(_.id)
    lazy val char10IdFk = foreignKey("char_10_id_fkey", char10Id, Char10PkTable)(_.id)
    lazy val varchar10IdFk = foreignKey("varchar_10_id_fkey", varchar10Id, Varchar10PkTable)(_.id)
  }

  lazy val ReferencingTable = new TableQuery(tag => new ReferencingTable(tag))
}

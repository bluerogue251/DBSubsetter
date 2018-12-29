package e2e.pktypes

import java.util.UUID

import slick.dbio.{DBIOAction, Effect, NoStream}

object PkTypesDML {
  def dbioSeq(ddl: PkTypesDDL): DBIOAction[Unit, NoStream, Effect.Write] ={
    import ddl._
    import ddl.profile.api._

    slick.dbio.DBIO.seq(
      BytePkTable ++= Seq(
        BytePkTableRow(-128),
        BytePkTableRow(0),
        BytePkTableRow(1),
        BytePkTableRow(127)
      ),
      ShortPkTable ++= Seq(
        ShortPkTableRow(-32768),
        ShortPkTableRow(-1),
        ShortPkTableRow(0),
        ShortPkTableRow(1),
        ShortPkTableRow(32767)
      ),
      IntPkTable ++= Seq(
        IntPkTableRow(-2147483648),
        IntPkTableRow(-1),
        IntPkTableRow(0),
        IntPkTableRow(1),
        IntPkTableRow(2147483647)
      ),
      LongPkTable ++= Seq(
        LongPkTableRow(-9223372036854775808L),
        LongPkTableRow(-1),
        LongPkTableRow(0),
        LongPkTableRow(1),
        LongPkTableRow(9223372036854775807L)
      ),
      UUIDPkTable ++= Seq(
        UUIDPkTableRow(UUID.fromString("ae2c53e6-bef2-42cb-aaf7-3bdd58b0b645")),
        UUIDPkTableRow(UUID.fromString("1607ce51-dcd1-480a-99a1-78027b654f50")),
        UUIDPkTableRow(UUID.fromString("c6bafc98-7f13-4421-9361-3f2ea8c26c67")),
        UUIDPkTableRow(UUID.fromString("9623ac02-13f4-4777-b984-39c5e275f710"))
      ),
      Char10PkTable ++= Seq(
        Char10PkTableRow("one"),
        Char10PkTableRow("two "),
        Char10PkTableRow(" three"),
        Char10PkTableRow(" four "),
        Char10PkTableRow("")
      ),
      Varchar10PkTable ++= Seq(
        Varchar10PkTableRow("five"),
        Varchar10PkTableRow("six "),
        Varchar10PkTableRow(" seven"),
        Varchar10PkTableRow(" eight "),
        Varchar10PkTableRow("")
      ),
      ReferencingTable ++= Seq(
        ReferencingTableRow(1, Some(-128), None, None, None, None, None, None),
        ReferencingTableRow(2, Some(0), None, None, None, None, None, None),
        ReferencingTableRow(3, Some(1), None, None, None, None, None, None),
        ReferencingTableRow(4, Some(127), None, None, None, None, None, None),
        ReferencingTableRow(5, None, Some(-32768), None, None, None, None, None),
        ReferencingTableRow(6, None, Some(0), None, None, None, None, None),
        ReferencingTableRow(7, None, Some(1), None, None, None, None, None),
        ReferencingTableRow(8, None, Some(32767), None, None, None, None, None),
        ReferencingTableRow(9, None, None, Some(-2147483648), None, None, None, None),
        ReferencingTableRow(10, None, None, Some(0), None, None, None, None),
        ReferencingTableRow(11, None, None, Some(1), None, None, None, None),
        ReferencingTableRow(12, None, None, Some(2147483647), None, None, None, None),
        ReferencingTableRow(13, None, None, None, Some(-9223372036854775808L), None, None, None),
        ReferencingTableRow(14, None, None, None, Some(0), None, None, None),
        ReferencingTableRow(15, None, None, None, Some(1), None, None, None),
        ReferencingTableRow(16, None, None, None, Some(9223372036854775807L), None, None, None),
        ReferencingTableRow(17, None, None, None, None, Some(UUID.fromString("ae2c53e6-bef2-42cb-aaf7-3bdd58b0b645")), None, None),
        ReferencingTableRow(18, None, None, None, None, Some(UUID.fromString("1607ce51-dcd1-480a-99a1-78027b654f50")), None, None),
        ReferencingTableRow(19, None, None, None, None, None, Some("one"), None),
        ReferencingTableRow(20, None, None, None, None, None, Some("two "), None),
        ReferencingTableRow(21, None, None, None, None, None, Some(" four "), None),
        ReferencingTableRow(22, None, None, None, None, None, None, Some("five")),
        ReferencingTableRow(23, None, None, None, None, None, None, Some("six ")),
        ReferencingTableRow(24, None, None, None, None, None, None, Some(" eight "))
      )
    )
  }
}

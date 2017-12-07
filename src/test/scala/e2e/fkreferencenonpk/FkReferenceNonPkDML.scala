package e2e.fkreferencenonpk

import slick.jdbc.JdbcProfile

class FkReferenceNonPkDML(val profile: JdbcProfile) extends FkReferenceNonPkDDL {

  import profile.api._

  def dbioSeq = {

    slick.dbio.DBIO.seq(
      ReferencedTable ++= Seq(
        ReferencedTableRow(1, "key1"),
        ReferencedTableRow(2, "key2"),
        ReferencedTableRow(3, "key3"),
        ReferencedTableRow(4, "key4"),
        ReferencedTableRow(5, "key5")
      ),
      ReferencingTable ++= Seq(
        ReferencingTableRow(1, "key1"),
        ReferencingTableRow(2, "key1"),
        ReferencingTableRow(3, "key2"),
        ReferencingTableRow(4, "key1"),
        ReferencingTableRow(5, "key5"),
        ReferencingTableRow(6, "key2"),
        ReferencingTableRow(7, "key1"),
        ReferencingTableRow(8, "key1"),
        ReferencingTableRow(9, "key4")
      )
    )
  }
}

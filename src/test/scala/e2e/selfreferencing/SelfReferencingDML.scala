package e2e.selfreferencing

import slick.jdbc.JdbcProfile

class SelfReferencingDML(val profile: JdbcProfile) extends SelfReferencingDDL {

  import profile.api._

  def dbioSeq = {
    slick.dbio.DBIO.seq(
      SelfReferencingTable ++= Seq(
        // edge case: it references not only its own table, but its own row in that table
        SelfReferencingTableRow(1, 1),
        SelfReferencingTableRow(2, 1),
        SelfReferencingTableRow(3, 2),
        SelfReferencingTableRow(4, 7),
        SelfReferencingTableRow(5, 1),
        SelfReferencingTableRow(6, 3),
        SelfReferencingTableRow(7, 4),
        SelfReferencingTableRow(8, 1),
        SelfReferencingTableRow(9, 1),
        SelfReferencingTableRow(10, 2),
        SelfReferencingTableRow(11, 3),
        SelfReferencingTableRow(12, 4),
        SelfReferencingTableRow(13, 5),
        SelfReferencingTableRow(14, 6),
        SelfReferencingTableRow(15, 7),
        SelfReferencingTableRow(16, 15)
      )
    )
  }
}

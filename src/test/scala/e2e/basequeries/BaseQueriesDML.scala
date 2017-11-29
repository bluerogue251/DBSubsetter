package e2e.basequeries

import slick.jdbc.JdbcProfile

class BaseQueriesDML(val profile: JdbcProfile) extends BaseQueriesDDL {

  import profile.api._

  def dbioSeq = {
    slick.dbio.DBIO.seq(
      BaseTable ++= (1 to 10).map(BaseTableRow),
      ChildTable ++= Seq(
        ChildTableRow(1, 1),
        ChildTableRow(2, 1),
        ChildTableRow(3, 2),
        ChildTableRow(4, 2),
        ChildTableRow(5, 3),
        ChildTableRow(6, 3),
        ChildTableRow(7, 3),
        ChildTableRow(8, 3),
        ChildTableRow(9, 4),
        ChildTableRow(10, 4),
        ChildTableRow(11, 5),
        ChildTableRow(12, 5),
        ChildTableRow(13, 6),
        ChildTableRow(14, 6),
        ChildTableRow(15, 7))
    )
  }
}

package e2e.missingfk

import slick.jdbc.JdbcProfile

abstract class Inserts(prof: JdbcProfile) extends Tables {

  import prof.api._

  def dbioSeq = {

    slick.dbio.DBIO.seq(
      Table1 ++= Seq(
        Table1Row(1),
        Table1Row(2)
      ),
      Table2 ++= Seq(
        Table2Row(1, 2),
        Table2Row(2, 2),
        Table2Row(3, 1)
      ),
      Table3 ++= Seq(
        Table3Row(45),
        Table3Row(46),
        Table3Row(47),
        Table3Row(48),
        Table3Row(49),
        Table3Row(50)
      ),
      Table4 ++= Seq(
        Table4Row(2, 45),
        Table4Row(1, 47),
        Table4Row(2, 50)
      ),
      Table5 ++= Seq(
        Table5Row(98, 1, 47),
        Table5Row(99, 2, 45)
      ),
      TableA ++= Seq(
        TableARow(1, "points_to_table_b", 1),
        TableARow(2, "points_to_table_b", 1),
        TableARow(3, "points_to_table_b", 2),
        TableARow(4, "points_to_table_d", 2),
        // edge case -- id does not exist in target table
        TableARow(5, "points_to_table_d", 30),
        // edge case -- Row 6 is NOT part of the subset, so row 1 of table_d should NOT be included
        // however, row #1 of table_b SHOULD be included.
        // This helps to test that table_b subsetting is not accidentally leaking over into table_d
        // This is based on a real bug that used to exist in our code
        TableARow(6, "points_to_table_d", 1)
      ),
      TableB ++= Seq(
        TableBRow(1),
        TableBRow(2),
        TableBRow(3)
      ),
      TableC ++= Seq(
        TableCRow(1),
        TableCRow(2),
        TableCRow(3)
      ),
      TableD ++= Seq(
        TableDRow(1),
        TableDRow(2),
        TableDRow(3)
      )
    )
  }
}

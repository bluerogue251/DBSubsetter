package trw.dbsubsetter.e2e.basequeries

import slick.dbio.{DBIOAction, Effect, NoStream}

object BaseQueriesDML {
  def dbioSeq(ddl: BaseQueriesDDL): DBIOAction[Unit, NoStream, Effect.Write] = {
    import ddl.profile.api._
    slick.dbio.DBIO.seq(
      ddl.BaseTable ++= (1 to 10).map(ddl.BaseTableRow),
      ddl.ChildTable ++= Seq(
        ddl.ChildTableRow(1, 1),
        ddl.ChildTableRow(2, 1),
        ddl.ChildTableRow(3, 2),
        ddl.ChildTableRow(4, 2),
        ddl.ChildTableRow(5, 3),
        ddl.ChildTableRow(6, 3),
        ddl.ChildTableRow(7, 3),
        ddl.ChildTableRow(8, 3),
        ddl.ChildTableRow(9, 4),
        ddl.ChildTableRow(10, 4),
        ddl.ChildTableRow(11, 5),
        ddl.ChildTableRow(12, 5),
        ddl.ChildTableRow(13, 6),
        ddl.ChildTableRow(14, 6),
        ddl.ChildTableRow(15, 7))
    )
  }
}

package e2e.compositekeys

import slick.dbio.{DBIOAction, Effect, NoStream}

object CompositeKeysDML {
  def dbioSeq(ddl: CompositeKeysDDL): DBIOAction[Unit, NoStream, Effect.Write] = {
    import ddl._
    import ddl.profile.api._
    slick.dbio.DBIO.seq(
      ParentTable ++= Seq(
        ParentTableRow(1, 2),
        ParentTableRow(3, 4),
        ParentTableRow(5, 6)
      ),
      ChildTable ++= Seq(
        ChildTableRow(100, 1, 2),
        ChildTableRow(101, 1, 2),
        ChildTableRow(102, 1, 2),
        ChildTableRow(103, 3, 4),
        ChildTableRow(104, 3, 4),
        ChildTableRow(105, 3, 4),
        ChildTableRow(106, 5, 6),
        ChildTableRow(107, 5, 6),
        ChildTableRow(108, 5, 6)
      )
    )
  }
}

package trw.dbsubsetter.e2e.fktononpk

import slick.dbio.{DBIOAction, Effect, NoStream}

object ForeignKeyToNonPrimaryKeyDML {
  def dbioSeq(ddl: ForeignKeyToNonPrimaryKeyDDL): DBIOAction[Unit, NoStream, Effect.Write] = {
    import ddl.profile.api._

    slick.dbio.DBIO.seq(
      ddl.ReferencedTable ++= Seq(
        ddl.ReferencedTableRow(1, "key1"),
        ddl.ReferencedTableRow(2, "key2"),
        ddl.ReferencedTableRow(3, "key3"),
        ddl.ReferencedTableRow(4, "key4"),
        ddl.ReferencedTableRow(5, "key5")
      ),
      ddl.ReferencingTable ++= Seq(
        ddl.ReferencingTableRow(1, "key1"),
        ddl.ReferencingTableRow(2, "key1"),
        ddl.ReferencingTableRow(3, "key2"),
        ddl.ReferencingTableRow(4, "key1"),
        ddl.ReferencingTableRow(5, "key5"),
        ddl.ReferencingTableRow(6, "key2"),
        ddl.ReferencingTableRow(7, "key1"),
        ddl.ReferencingTableRow(8, "key1"),
        ddl.ReferencingTableRow(9, "key4")
      )
    )
  }
}

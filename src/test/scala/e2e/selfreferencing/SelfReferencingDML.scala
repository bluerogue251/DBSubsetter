package e2e.selfreferencing

import slick.dbio.{DBIOAction, Effect, NoStream}

object SelfReferencingDML {
  def dbioSeq(ddl: SelfReferencingDDL): DBIOAction[Unit, NoStream, Effect.Write] = {
    import ddl._
    import ddl.profile.api._

    slick.dbio.DBIO.seq(
      SelfReferencingTable ++= Seq(
        // edge case: it references not only its own table, but its own row in that table
        SelfReferencingTableRow(1, None), // 1),
        SelfReferencingTableRow(2, None), // 1),
        SelfReferencingTableRow(3, None), // 2),
        SelfReferencingTableRow(4, None), // 7),
        SelfReferencingTableRow(5, None), // 1),
        SelfReferencingTableRow(6, None), // 3),
        SelfReferencingTableRow(7, None), // 4),
        SelfReferencingTableRow(8, None), // 1),
        SelfReferencingTableRow(9, None), // 1),
        SelfReferencingTableRow(10, None), //  2),
        SelfReferencingTableRow(11, None), //  3),
        SelfReferencingTableRow(12, None), //  4),
        SelfReferencingTableRow(13, None), //  5),
        SelfReferencingTableRow(14, None), //  6),
        SelfReferencingTableRow(15, None), //  7),
        SelfReferencingTableRow(16, None) //  15)
      ),
      SelfReferencingTable.filter(_.id === 1).map(_.parentId).update(Some(1)),
      SelfReferencingTable.filter(_.id === 2).map(_.parentId).update(Some(1)),
      SelfReferencingTable.filter(_.id === 3).map(_.parentId).update(Some(2)),
      SelfReferencingTable.filter(_.id === 4).map(_.parentId).update(Some(7)),
      SelfReferencingTable.filter(_.id === 5).map(_.parentId).update(Some(1)),
      SelfReferencingTable.filter(_.id === 6).map(_.parentId).update(Some(3)),
      SelfReferencingTable.filter(_.id === 7).map(_.parentId).update(Some(4)),
      SelfReferencingTable.filter(_.id === 8).map(_.parentId).update(Some(1)),
      SelfReferencingTable.filter(_.id === 9).map(_.parentId).update(Some(1)),
      SelfReferencingTable.filter(_.id === 10).map(_.parentId).update(Some(2)),
      SelfReferencingTable.filter(_.id === 11).map(_.parentId).update(Some(3)),
      SelfReferencingTable.filter(_.id === 12).map(_.parentId).update(Some(4)),
      SelfReferencingTable.filter(_.id === 13).map(_.parentId).update(Some(5)),
      SelfReferencingTable.filter(_.id === 14).map(_.parentId).update(Some(6)),
      SelfReferencingTable.filter(_.id === 15).map(_.parentId).update(Some(7)),
      SelfReferencingTable.filter(_.id === 16).map(_.parentId).update(Some(15))
    )
  }
}

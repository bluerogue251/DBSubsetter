package e2e.circulardep

import slick.dbio.{DBIOAction, Effect, NoStream}

object CircularDepDML {

  def dbioSeq(ddl: CircularDepDDL): DBIOAction[Unit, NoStream, Effect.Write] = {
    import ddl.profile.api._

    val insertStatements = Seq(
      ddl.Grandparents ++= (0 to 10).map(i => ddl.GrandparentsRow(i, None)),
      ddl.Parents ++= (0 to 109).map(i => ddl.ParentsRow(i, i / 10)),
      ddl.Children ++= (0 to 549).map(i => ddl.ChildrenRow(i, i / 5))
    )

    val updateStatements = (0 to 10 by 3).map { i =>
      ddl.Grandparents.filter(_.id === i).map(_.favoriteParentId).update(Some(i * 10))
    }

    val allStatements = insertStatements ++ updateStatements
    slick.dbio.DBIO.seq(allStatements: _*)
  }
}



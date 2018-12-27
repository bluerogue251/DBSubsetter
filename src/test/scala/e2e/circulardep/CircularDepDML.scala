package e2e.circulardep

import slick.jdbc.JdbcProfile

class CircularDepDML(val profile: JdbcProfile) extends CircularDepDDL {

  import profile.api._

  def dbioSeq = {
    val insertStatements = Seq(
      Grandparents ++= (0 to 10).map(i => GrandparentsRow(i, None)),
      Parents ++= (0 to 109).map(i => ParentsRow(i, i / 10)),
      Children ++= (0 to 549).map(i => ChildrenRow(i, i / 5))
    )
    val updateStatements = (0 to 10 by 3).map { i =>
      Grandparents.filter(_.id === i).map(_.favoriteParentId).update(Some(i * 10))
    }
    val allStatements = insertStatements ++ updateStatements
    slick.dbio.DBIO.seq(allStatements: _*)
  }
}



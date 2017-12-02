package e2e.autoincrementingpk

import slick.jdbc.JdbcProfile

class AutoIncrementingPkDML(val profile: JdbcProfile) extends AutoIncrementingPkDDL {

  import profile.api._

  def dbioSeq = {
    slick.dbio.DBIO.seq(
      AutoincrementingPkTable ++= (1 to 20).map(i => AutoincrementingPkTableRow(i, i.toString)),
      OtherAutoincrementingPkTable ++= (1 to 20).map(i => OtherAutoincrementingPkTableRow(i, i.toString))
    )
  }
}

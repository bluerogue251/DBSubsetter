package e2e.autoincrementingpk

class AutoIncrementingPkDML(ddl: AutoIncrementingPkDDL) {

  import ddl.profile.api._

  def dbioSeq = {
    slick.dbio.DBIO.seq(
      ddl.AutoincrementingPkTable ++= (1 to 20).map(i => ddl.AutoincrementingPkTableRow(i, i.toString)),
      ddl.OtherAutoincrementingPkTable ++= (1 to 20).map(i => ddl.OtherAutoincrementingPkTableRow(i, i.toString))
    )
  }
}

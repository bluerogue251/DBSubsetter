package trw.dbsubsetter.e2e.autoincrementingpk

import slick.dbio.{DBIOAction, Effect, NoStream}

object AutoIncrementingPkDML {
  def dbioSeq(ddl: AutoIncrementingPkDDL): DBIOAction[Unit, NoStream, Effect.Write] = {
    import ddl.profile.api._
    slick.dbio.DBIO.seq(
      ddl.AutoincrementingPkTable ++= (1 to 20).map(i => ddl.AutoincrementingPkTableRow(i, i.toString)),
      ddl.OtherAutoincrementingPkTable ++= (1 to 20).map(i => ddl.OtherAutoincrementingPkTableRow(i, i.toString))
    )
  }
}

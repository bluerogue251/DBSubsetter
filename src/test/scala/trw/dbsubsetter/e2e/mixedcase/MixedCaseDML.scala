package trw.dbsubsetter.e2e.mixedcase

import slick.dbio.{DBIOAction, Effect, NoStream}

object MixedCaseDML {
  def dbioSeq(ddl: MixedCaseDDL): DBIOAction[Unit, NoStream, Effect.Write] = {
    import ddl._
    import ddl.profile.api._
    slick.dbio.DBIO.seq(
      MixedCaseTable1 ++= Seq(
        MixedCaseTable1Row(1),
        MixedCaseTable1Row(2),
        MixedCaseTable1Row(3)
      ),
      MixedCaseTable2 ++= Seq(
        MixedCaseTable2Row(1, 1),
        MixedCaseTable2Row(2, 1),
        MixedCaseTable2Row(3, 1),
        MixedCaseTable2Row(4, 2),
        MixedCaseTable2Row(5, 2),
        MixedCaseTable2Row(6, 2),
        MixedCaseTable2Row(7, 3),
        MixedCaseTable2Row(8, 3),
        MixedCaseTable2Row(9, 3)
      )
    )
  }
}

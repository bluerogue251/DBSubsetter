package trw.dbsubsetter.e2e.mixedcase

import trw.dbsubsetter.e2e.AbstractSqlServerEndToEndTest

class MixedCaseTestSqlServer extends AbstractSqlServerEndToEndTest with MixedCaseTest {

  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.mixed_CASE_table_1 ::: [ID] = 2 ::: includeChildren",
    "--skipPkStore", "dbo.mixed_CASE_table_1",
    "--skipPkStore", "dbo.mixed_CASE_table_2"
  )
}

package trw.dbsubsetter.e2e.mixedcase

import trw.dbsubsetter.e2e.AbstractMysqlEndToEndTest

class MixedCaseTestMySql extends AbstractMysqlEndToEndTest with MixedCaseTest {

  override val programArgs = Array(
    "--schemas", "mIXED_case_DB",
    "--baseQuery", "mIXED_case_DB.mixed_CASE_table_1 ::: `ID` = 2 ::: includeChildren",
    "--skipPkStore", "mIXED_case_DB.mixed_CASE_table_1",
    "--skipPkStore", "mIXED_case_DB.mixed_CASE_table_2"
  )
}

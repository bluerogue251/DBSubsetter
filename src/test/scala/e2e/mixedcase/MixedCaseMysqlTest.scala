package e2e.mixedcase

import e2e.AbstractMysqlEndToEndTest

class MixedCaseMysqlTest extends AbstractMysqlEndToEndTest with MixedCaseTestCases {
  override val originPort = 5530
  override val programArgs = Array(
    "--schemas", "mIXED_case_DB",
    "--baseQuery", "mIXED_case_DB.mixed_CASE_table_1 ::: `ID` = 2 ::: true"
  )
}

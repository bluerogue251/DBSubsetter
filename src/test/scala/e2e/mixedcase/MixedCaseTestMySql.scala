package e2e.mixedcase

import e2e.MySqlEnabledTest

class MixedCaseTestMySql extends MySqlEnabledTest with MixedCaseTest {

  override val programArgs = Array(
    "--schemas", "mIXED_case_DB",
    "--baseQuery", "mIXED_case_DB.mixed_CASE_table_1 ::: `ID` = 2 ::: includeChildren"
  )
}

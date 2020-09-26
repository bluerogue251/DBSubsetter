package e2e.mixedcase

import e2e.SqlServerSubsettingTest

class MixedCaseTestSqlServer extends SqlServerSubsettingTest with MixedCaseTest {

  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.mixed_CASE_table_1 ::: [ID] = 2 ::: includeChildren"
  )
}

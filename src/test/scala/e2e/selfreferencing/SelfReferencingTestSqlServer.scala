package e2e.selfreferencing

import e2e.SqlServerSubsettingTest

class SelfReferencingTestSqlServer extends SqlServerSubsettingTest with SelfReferencingTest {

  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}

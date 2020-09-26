package e2e.selfreferencing

import e2e.SqlServerEnabledTest

class SelfReferencingTestSqlServer extends SqlServerEnabledTest with SelfReferencingTest {

  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}

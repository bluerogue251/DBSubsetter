package e2e.basequeries

import e2e.SqlServerEnabledTest

class BaseQueriesTestSqlServer extends SqlServerEnabledTest with BaseQueriesTest {

  override protected val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.base_table ::: 1 = 1 ::: excludeChildren"
  )
}

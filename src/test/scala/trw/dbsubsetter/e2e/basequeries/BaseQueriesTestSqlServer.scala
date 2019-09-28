package trw.dbsubsetter.e2e.basequeries

import trw.dbsubsetter.e2e.AbstractSqlServerEndToEndTest

class BaseQueriesTestSqlServer extends AbstractSqlServerEndToEndTest with BaseQueriesTest {

  override protected val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.base_table ::: 1 = 1 ::: excludeChildren"
  )
}

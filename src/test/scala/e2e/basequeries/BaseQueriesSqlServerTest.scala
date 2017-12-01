package e2e.basequeries

import e2e.AbstractSqlServerEndToEndTest

class BaseQueriesSqlServerTest extends AbstractSqlServerEndToEndTest with BaseQueriesTestCases {
  override val originPort = 5516
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.base_table ::: true ::: false"
  )
}

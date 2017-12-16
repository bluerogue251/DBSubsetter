package e2e.pktypes

import e2e.AbstractSqlServerEndToEndTest

class PkTypesSqlServerTest extends AbstractSqlServerEndToEndTest with PkTypesTestCases {
  override val originPort = 5576
  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.mixed_CASE_table_1 ::: [ID] = 2 ::: includeChildren",
    "--skipPkStore", "dbo.mixed_CASE_table_1",
    "--skipPkStore", "dbo.mixed_CASE_table_2"
  )
}

package e2e.circulardep

import e2e.AbstractSqlServerEndToEndTest

class CircularDepSqlServerTest extends AbstractSqlServerEndToEndTest with CircularDepTestCases {
  override val originPort = 5486
  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.grandparents ::: id % 6 = 0 ::: includeChildren",
    "--skipPkStore", "dbo.children"
  )
}

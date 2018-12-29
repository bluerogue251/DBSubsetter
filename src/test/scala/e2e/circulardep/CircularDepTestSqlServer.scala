package e2e.circulardep

import e2e.AbstractSqlServerEndToEndTest

class CircularDepTestSqlServer extends AbstractSqlServerEndToEndTest with CircularDepTest {
  override val port = 5486
  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.grandparents ::: id % 6 = 0 ::: includeChildren",
    "--skipPkStore", "dbo.children"
  )
}

package e2e.selfreferencing

import e2e.AbstractSqlServerEndToEndTest

class SelfReferencingTestSqlServer extends AbstractSqlServerEndToEndTest with SelfReferencingTest {
  override val port = 5526
  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}

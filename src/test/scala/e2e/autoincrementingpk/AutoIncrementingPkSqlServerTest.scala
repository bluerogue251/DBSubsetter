package e2e.autoincrementingpk

import e2e.AbstractSqlServerEndToEndTest

class AutoIncrementingPkSqlServerTest extends AbstractSqlServerEndToEndTest with AutoIncrementingPkTestCases {
  override val originPort = 5556
  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id % 2 = 0 ::: true"
  )
}

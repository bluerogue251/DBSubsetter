package e2e.autoincrementingpk

import e2e.AbstractMysqlEndToEndTest

class AutoIncrementingPkMysqlTest extends AbstractMysqlEndToEndTest with AutoIncrementingPkTestCases {
  override val originPort = 5550
  override val programArgs = Array(
    "--schemas", "autoincrementing_pk",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id % 2 = 0 ::: true"
  )
}

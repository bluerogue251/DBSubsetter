package e2e.autoincrementingpk

import e2e.AbstractPostgresqlEndToEndTest

class AutoIncrementingPkPostgresqlTest extends AbstractPostgresqlEndToEndTest with AutoIncrementingPkTestCases {
  override val originPort = 5553
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.autoincrementing_pk_table ::: id % 2 = 0 ::: true"
  )
}

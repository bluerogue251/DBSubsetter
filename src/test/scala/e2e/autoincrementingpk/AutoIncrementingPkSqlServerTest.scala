package e2e.autoincrementingpk

import e2e.AbstractSqlServerEndToEndTest

class AutoIncrementingPkSqlServerTest extends AbstractSqlServerEndToEndTest with AutoIncrementingPkTestCases {
  override val originPort = 5556
  override val programArgs = Array(
    "--schemas", "dbo",

    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 2 ::: true",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 4 ::: true",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 6 ::: true",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 8 ::: true",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 10 ::: true",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 12 ::: true",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 14 ::: true",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 16 ::: true",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 18 ::: true",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 20 ::: true",

    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 2 ::: true",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 4 ::: true",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 6 ::: true",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 8 ::: true",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 10 ::: true",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 12 ::: true",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 14 ::: true",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 16 ::: true",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 18 ::: true",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 20 ::: true"
  )
}

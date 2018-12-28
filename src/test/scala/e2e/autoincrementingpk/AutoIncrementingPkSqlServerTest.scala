package e2e.autoincrementingpk

import e2e.AbstractSqlServerEndToEndTest

class AutoIncrementingPkSqlServerTest extends AbstractSqlServerEndToEndTest with AutoIncrementingPkTestCases {
  override val port = 5556
  override val programArgs = Array(
    "--schemas", "dbo",

    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 2 ::: includeChildren",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 4 ::: includeChildren",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 6 ::: includeChildren",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 8 ::: includeChildren",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 10 ::: includeChildren",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 12 ::: includeChildren",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 14 ::: includeChildren",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 16 ::: includeChildren",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 18 ::: includeChildren",
    "--baseQuery", "dbo.autoincrementing_pk_table ::: id = 20 ::: includeChildren",

    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 2 ::: includeChildren",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 4 ::: includeChildren",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 6 ::: includeChildren",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 8 ::: includeChildren",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 10 ::: includeChildren",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 12 ::: includeChildren",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 14 ::: includeChildren",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 16 ::: includeChildren",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 18 ::: includeChildren",
    "--baseQuery", "dbo.other_autoincrementing_pk_table ::: id = 20 ::: includeChildren"
  )
}

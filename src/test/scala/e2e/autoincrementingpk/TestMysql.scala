package e2e.autoincrementingpk

import e2e.AbstractMysqlEndToEndTest

class TestMysql extends AbstractMysqlEndToEndTest with AutoIncrementingPkTest {
  override protected val originPort = 5550

  override protected val programArgs = Array(
    "--schemas", "autoincrementing_pk",

    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 2 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 4 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 6 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 8 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 10 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 12 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 14 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 16 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 18 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 20 ::: includeChildren",

    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 2 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 4 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 6 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 8 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 10 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 12 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 14 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 16 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 18 ::: includeChildren",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 20 ::: includeChildren"
  )
}

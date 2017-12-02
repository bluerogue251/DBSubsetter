package e2e.autoincrementingpk

import e2e.AbstractMysqlEndToEndTest

class AutoIncrementingPkMysqlTest extends AbstractMysqlEndToEndTest with AutoIncrementingPkTestCases {
  override val originPort = 5550
  override val programArgs = Array(
    "--schemas", "autoincrementing_pk",

    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 2 ::: true",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 4 ::: true",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 6 ::: true",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 8 ::: true",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 10 ::: true",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 12 ::: true",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 14 ::: true",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 16 ::: true",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 18 ::: true",
    "--baseQuery", "autoincrementing_pk.autoincrementing_pk_table ::: id = 20 ::: true",

    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 2 ::: true",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 4 ::: true",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 6 ::: true",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 8 ::: true",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 10 ::: true",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 12 ::: true",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 14 ::: true",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 16 ::: true",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 18 ::: true",
    "--baseQuery", "autoincrementing_pk.other_autoincrementing_pk_table ::: id = 20 ::: true"
  )
}

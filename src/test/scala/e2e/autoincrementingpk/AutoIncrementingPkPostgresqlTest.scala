package e2e.autoincrementingpk

import e2e.AbstractPostgresqlEndToEndTest

class AutoIncrementingPkPostgresqlTest extends AbstractPostgresqlEndToEndTest with AutoIncrementingPkTestCases {
  override val originPort = 5553
  override val programArgs = Array(
    "--schemas", "public",

    "--baseQuery", "public.autoincrementing_pk_table ::: id = 2 ::: true",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 4 ::: true",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 6 ::: true",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 8 ::: true",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 10 ::: true",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 12 ::: true",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 14 ::: true",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 16 ::: true",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 18 ::: true",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 20 ::: true",

    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 2 ::: true",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 4 ::: true",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 6 ::: true",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 8 ::: true",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 10 ::: true",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 12 ::: true",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 14 ::: true",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 16 ::: true",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 18 ::: true",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 20 ::: true"
  )
}

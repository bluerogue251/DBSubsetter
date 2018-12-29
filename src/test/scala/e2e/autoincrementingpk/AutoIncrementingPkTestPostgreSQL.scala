package e2e.autoincrementingpk

import e2e.AbstractPostgresqlEndToEndTest

class AutoIncrementingPkTestPostgreSQL extends AbstractPostgresqlEndToEndTest with AutoIncrementingPkTest {
  override protected val originPort = 5553

  override protected val programArgs = Array(
    "--schemas", "public",

    "--baseQuery", "public.autoincrementing_pk_table ::: id = 2 ::: includeChildren",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 4 ::: includeChildren",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 6 ::: includeChildren",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 8 ::: includeChildren",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 10 ::: includeChildren",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 12 ::: includeChildren",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 14 ::: includeChildren",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 16 ::: includeChildren",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 18 ::: includeChildren",
    "--baseQuery", "public.autoincrementing_pk_table ::: id = 20 ::: includeChildren",

    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 2 ::: includeChildren",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 4 ::: includeChildren",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 6 ::: includeChildren",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 8 ::: includeChildren",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 10 ::: includeChildren",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 12 ::: includeChildren",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 14 ::: includeChildren",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 16 ::: includeChildren",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 18 ::: includeChildren",
    "--baseQuery", "public.other_autoincrementing_pk_table ::: id = 20 ::: includeChildren"
  )
}

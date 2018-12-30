package e2e.mixedcase

import e2e.AbstractPostgresqlEndToEndTest

class MixedCaseTestPostgreSQL extends AbstractPostgresqlEndToEndTest with MixedCaseTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.mixed_CASE_table_1 ::: \"ID\" = 2 ::: includeChildren",
    "--skipPkStore", "public.mixed_CASE_table_1",
    "--skipPkStore", "public.mixed_CASE_table_2"
  )
}

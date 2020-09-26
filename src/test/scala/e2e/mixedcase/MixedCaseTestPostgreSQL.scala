package e2e.mixedcase

import e2e.PostgresEnabledTest

class MixedCaseTestPostgreSQL extends PostgresEnabledTest with MixedCaseTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.mixed_CASE_table_1 ::: \"ID\" = 2 ::: includeChildren"
  )
}

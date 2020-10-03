package e2e.mixedcase

import e2e.PostgresSubsettingTest

class MixedCaseTestPostgres extends PostgresSubsettingTest with MixedCaseTest {
  // format: off

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.mixed_CASE_table_1 ::: \"ID\" = 2 ::: includeChildren"
  )
}

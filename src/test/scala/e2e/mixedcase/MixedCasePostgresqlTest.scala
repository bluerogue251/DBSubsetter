package e2e.mixedcase

import e2e.AbstractPostgresqlEndToEndTest

class MixedCasePostgresqlTest extends AbstractPostgresqlEndToEndTest with MixedCaseTestCases {
  override val originPort = 5533
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.mixed_CASE_table_1 ::: \"ID\" = 2 ::: includeChildren"
  )
}

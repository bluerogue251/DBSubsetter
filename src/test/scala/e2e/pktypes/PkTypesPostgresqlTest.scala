package e2e.pktypes

import e2e.AbstractPostgresqlEndToEndTest

class PkTypesPostgresqlTest extends AbstractPostgresqlEndToEndTest with PkTypesTestCases {
  override val originPort = 5573
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.mixed_CASE_table_1 ::: \"ID\" = 2 ::: includeChildren",
    "--skipPkStore", "public.mixed_CASE_table_1",
    "--skipPkStore", "public.mixed_CASE_table_2"
  )
}

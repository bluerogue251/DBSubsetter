package e2e.pktypes

import e2e.AbstractPostgresqlEndToEndTest

class PkTypesPostgresqlTest extends AbstractPostgresqlEndToEndTest with PkTypesTestCases {
  override protected val originPort = 5573

  override protected val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.byte_pks ::: id = -128 ::: includeChildren",
    "--baseQuery", "public.short_pks ::: id = -32768 ::: includeChildren",
    "--baseQuery", "public.int_pks ::: id = -2147483648 ::: includeChildren",
    "--baseQuery", "public.long_pks ::: id = -9223372036854775808 ::: includeChildren",
    "--baseQuery", "public.uuid_pks ::: id = 'ae2c53e6-bef2-42cb-aaf7-3bdd58b0b645' ::: includeChildren",
    "--baseQuery", "public.char_10_pks ::: id = 'two' ::: includeChildren",
    "--baseQuery", "public.varchar_10_pks ::: id = 'six ' ::: includeChildren",
    "--baseQuery", "public.referencing_table ::: id in (3, 4, 7, 8, 11, 12, 15, 16, 18, 21, 24) ::: includeChildren"
  )
}

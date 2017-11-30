package e2e.missingfk

import e2e.AbstractPostgresqlEndToEndTest

class MissingFkPostgresqlTest extends AbstractPostgresqlEndToEndTest with MissingFkTestCases {
  override val originPort = 5493
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.table_1 ::: id = 2 ::: true",
    "--foreignKey", "public.table_2(table_1_id) ::: public.table_1(id)",
    "--primaryKey", "public.table_4(table_1_id, table_3_id)"
  )
}
package e2e.missingfk

import e2e.PostgresSubsettingTest

class MissingKeysTestPostgres extends PostgresSubsettingTest with MissingKeysTest {
  // format: off

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.table_1 ::: id = 2 ::: includeChildren",
    "--foreignKey", "public.table_2(table_1_id) ::: public.table_1(id)",
    "--primaryKey", "public.table_4(table_1_id, table_3_id)",
    "--excludeTable", "public.table_6"
  )
}

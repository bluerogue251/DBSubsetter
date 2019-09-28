package trw.dbsubsetter.e2e.missingfk

import trw.dbsubsetter.e2e.AbstractPostgresqlEndToEndTest

class MissingFkTestPostgreSQL extends AbstractPostgresqlEndToEndTest with MissingFkTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.table_1 ::: id = 2 ::: includeChildren",
    "--foreignKey", "public.table_2(table_1_id) ::: public.table_1(id)",
    "--primaryKey", "public.table_4(table_1_id, table_3_id)"
  )
}

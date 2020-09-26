package e2e.missingfk

import e2e.MySqlSubsettingTest

class MissingFkTestMySql extends MySqlSubsettingTest with MissingFkTest {

  override val programArgs = Array(
    "--schemas", "missing_fk",
    "--baseQuery", "missing_fk.table_1 ::: id = 2 ::: includeChildren",
    "--foreignKey", "missing_fk.table_2(table_1_id) ::: missing_fk.table_1(id)",
    "--primaryKey", "missing_fk.table_4(table_1_id, table_3_id)"
  )
}

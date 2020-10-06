package e2e.missingfk

import e2e.MySqlSubsettingTest

class MissingKeysTestMySql extends MySqlSubsettingTest with MissingKeysTest {
  // format: off
  
  override val programArgs = Array(
    "--schemas", "missing_keys",
    "--baseQuery", "missing_keys.table_1 ::: id = 2 ::: includeChildren",
    "--foreignKey", "missing_keys.table_2(table_1_id) ::: missing_fk.table_1(id)",
    "--primaryKey", "missing_keys.table_4(table_1_id, table_3_id)",
    "--excludeTable", "missing_keys.table_6"
  )
}

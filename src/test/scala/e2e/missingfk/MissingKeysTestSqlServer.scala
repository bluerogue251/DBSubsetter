package e2e.missingfk

import e2e.SqlServerSubsettingTest

class MissingKeysTestSqlServer extends SqlServerSubsettingTest with MissingKeysTest {

  override val programArgs = Array(
    "--schemas",
    "dbo",
    "--baseQuery",
    "dbo.table_1 ::: id = 2 ::: includeChildren",
    "--foreignKey",
    "dbo.table_2(table_1_id) ::: dbo.table_1(id)",
    "--primaryKey",
    "dbo.table_4(table_1_id, table_3_id)"
  )
}

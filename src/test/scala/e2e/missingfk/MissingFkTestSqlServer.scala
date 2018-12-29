package e2e.missingfk

import e2e.AbstractSqlServerEndToEndTest

class MissingFkTestSqlServer extends AbstractSqlServerEndToEndTest with MissingFkTest {
  override val port = 5496
  override val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.table_1 ::: id = 2 ::: includeChildren",
    "--foreignKey", "dbo.table_2(table_1_id) ::: dbo.table_1(id)",
    "--primaryKey", "dbo.table_4(table_1_id, table_3_id)"
  )
}

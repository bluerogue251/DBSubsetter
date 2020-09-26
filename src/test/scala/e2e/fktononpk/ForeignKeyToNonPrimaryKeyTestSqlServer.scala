package e2e.fktononpk

import e2e.SqlServerEnabledTest

class ForeignKeyToNonPrimaryKeyTestSqlServer extends SqlServerEnabledTest with ForeignKeyToNonPrimaryKeyTest {

  override protected val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.referenced_table ::: id in (1, 4) ::: includeChildren",
    "--baseQuery", "dbo.referencing_table ::: id = 5 ::: includeChildren"
  )
}

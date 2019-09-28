package trw.dbsubsetter.e2e.fktononpk

import trw.dbsubsetter.e2e.AbstractSqlServerEndToEndTest

class ForeignKeyToNonPrimaryKeyTestSqlServer extends AbstractSqlServerEndToEndTest with ForeignKeyToNonPrimaryKeyTest {

  override protected val programArgs = Array(
    "--schemas", "dbo",
    "--baseQuery", "dbo.referenced_table ::: id in (1, 4) ::: includeChildren",
    "--baseQuery", "dbo.referencing_table ::: id = 5 ::: includeChildren"
  )
}

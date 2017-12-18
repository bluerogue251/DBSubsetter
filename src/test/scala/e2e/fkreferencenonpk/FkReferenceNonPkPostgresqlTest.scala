package e2e.fkreferencenonpk

import e2e.AbstractPostgresqlEndToEndTest
import trw.dbsubsetter.db.Table

class FkReferenceNonPkPostgresqlTest extends AbstractPostgresqlEndToEndTest with FkReferenceNonPkTestCases {
  override val originPort = 5563
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.referenced_table ::: id in (1, 4) ::: includeChildren",
    "--baseQuery", "public.referencing_table ::: id = 5 ::: includeChildren"
  )

  // TODO generalize schema name so we can test more easily against different DB Vendors
  test("ForeignKey.pointsToPk") {
    val table = Table("public", "referencing_table", hasSqlServerAutoIncrement = false, storePks = true)
    val fk = schemaInfo.fksFromTable(table)
    assert(fk.lengthCompare(1) == 0)
    assert(!fk.head.pointsToPk)
  }
}

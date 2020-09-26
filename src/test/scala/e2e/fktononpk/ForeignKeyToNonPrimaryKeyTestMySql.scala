package e2e.fktononpk

import e2e.MySqlEnabledTest

class ForeignKeyToNonPrimaryKeyTestMySql extends MySqlEnabledTest with ForeignKeyToNonPrimaryKeyTest {

  override val programArgs = Array(
    "--schemas", "fk_reference_non_pk",
    "--baseQuery", "fk_reference_non_pk.referenced_table ::: id in (1, 4) ::: includeChildren",
    "--baseQuery", "fk_reference_non_pk.referencing_table ::: id = 5 ::: includeChildren"
  )
}

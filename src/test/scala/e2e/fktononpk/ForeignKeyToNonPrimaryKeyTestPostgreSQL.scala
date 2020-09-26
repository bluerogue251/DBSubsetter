package e2e.fktononpk

import e2e.PostgresSubsettingTest

class ForeignKeyToNonPrimaryKeyTestPostgreSQL extends PostgresSubsettingTest with ForeignKeyToNonPrimaryKeyTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.referenced_table ::: id in (1, 4) ::: includeChildren",
    "--baseQuery", "public.referencing_table ::: id = 5 ::: includeChildren"
  )
}

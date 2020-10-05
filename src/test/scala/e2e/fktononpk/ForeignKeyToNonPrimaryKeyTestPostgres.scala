package e2e.fktononpk

import e2e.PostgresSubsettingTest

class ForeignKeyToNonPrimaryKeyTestPostgres extends PostgresSubsettingTest with ForeignKeyToNonPrimaryKeyTest {
  // format: off

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.referenced_table ::: id in (1, 4) ::: includeChildren",
    "--baseQuery", "public.referencing_table ::: id = 5 ::: includeChildren",
    "--exposeMetrics"
  )
}

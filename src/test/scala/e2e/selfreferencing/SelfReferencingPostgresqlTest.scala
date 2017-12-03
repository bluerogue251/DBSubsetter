package e2e.selfreferencing

import e2e.AbstractPostgresqlEndToEndTest

class SelfReferencingPostgresqlTest extends AbstractPostgresqlEndToEndTest with SelfReferencingTestCases {
  override val originPort = 5523
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}

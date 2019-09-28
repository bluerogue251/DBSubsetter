package trw.dbsubsetter.e2e.selfreferencing

import trw.dbsubsetter.e2e.AbstractPostgresqlEndToEndTest

class SelfReferencingTestPostgreSQL extends AbstractPostgresqlEndToEndTest with SelfReferencingTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}

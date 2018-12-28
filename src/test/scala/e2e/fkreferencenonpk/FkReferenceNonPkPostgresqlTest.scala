package e2e.fkreferencenonpk

import e2e.AbstractPostgresqlEndToEndTest

class FkReferenceNonPkPostgresqlTest extends AbstractPostgresqlEndToEndTest with FkReferenceNonPkTestCases {
  override val originPort = 5563
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.referenced_table ::: id in (1, 4) ::: includeChildren",
    "--baseQuery", "public.referencing_table ::: id = 5 ::: includeChildren"
  )
}

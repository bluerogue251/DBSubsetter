package e2e.fkreferencenonpk

import e2e.AbstractMysqlEndToEndTest

class FkReferenceNonPkMysqlTest extends AbstractMysqlEndToEndTest with FkReferenceNonPkTestCases {
  override val originPort = 5560
  override val programArgs = Array(
    "--schemas", "fk_reference_non_pk",
    "--baseQuery", "fk_reference_non_pk.referenced_table ::: id in (1, 4) ::: includeChildren",
    "--baseQuery", "fk_reference_non_pk.referencing_table ::: id = 5 ::: includeChildren"
  )
}
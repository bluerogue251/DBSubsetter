package trw.dbsubsetter.e2e.selfreferencing

import trw.dbsubsetter.e2e.AbstractMysqlEndToEndTest

class SelfReferencingTestMySql extends AbstractMysqlEndToEndTest with SelfReferencingTest {

  override val programArgs = Array(
    "--schemas", "self_referencing",
    "--baseQuery", "self_referencing.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}

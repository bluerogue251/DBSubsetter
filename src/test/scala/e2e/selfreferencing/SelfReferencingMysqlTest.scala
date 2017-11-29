package e2e.selfreferencing

import e2e.AbstractMysqlEndToEndTest

class SelfReferencingMysqlTest extends AbstractMysqlEndToEndTest with SelfReferencingTestCases {
  override val profile = slick.jdbc.MySQLProfile
  override val originPort = 5520
  override val programArgs = Array(
    "--schemas", "self_referencing",
    "--baseQuery", "self_referencing.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: true"
  )
}

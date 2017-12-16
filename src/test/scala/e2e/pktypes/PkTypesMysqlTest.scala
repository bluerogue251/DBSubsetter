package e2e.pktypes

import e2e.AbstractMysqlEndToEndTest

class PkTypesMysqlTest extends AbstractMysqlEndToEndTest with PkTypesTestCases {
  override val originPort = 5570
  override val programArgs = Array(
    "--schemas", "pk_types",
    "--baseQuery", "pk_types.mixed_CASE_table_1 ::: `ID` = 2 ::: includeChildren",
    "--skipPkStore", "pk_types.mixed_CASE_table_1",
    "--skipPkStore", "pk_types.mixed_CASE_table_2"
  )
}

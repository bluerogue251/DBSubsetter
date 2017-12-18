//package e2e.mixedcase
//
//import e2e.AbstractSqlServerEndToEndTest
//
//class MixedCaseSqlServerTest extends AbstractSqlServerEndToEndTest with MixedCaseTestCases {
//  override val originPort = 5536
//  override val programArgs = Array(
//    "--schemas", "dbo",
//    "--baseQuery", "dbo.mixed_CASE_table_1 ::: [ID] = 2 ::: includeChildren",
//    "--skipPkStore", "dbo.mixed_CASE_table_1",
//    "--skipPkStore", "dbo.mixed_CASE_table_2"
//  )
//}

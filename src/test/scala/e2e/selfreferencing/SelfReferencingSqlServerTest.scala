//package e2e.selfreferencing
//
//import e2e.AbstractSqlServerEndToEndTest
//
//class SelfReferencingSqlServerTest extends AbstractSqlServerEndToEndTest with SelfReferencingTestCases {
//  override val originPort = 5526
//  override val programArgs = Array(
//    "--schemas", "dbo",
//    "--baseQuery", "dbo.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
//  )
//}

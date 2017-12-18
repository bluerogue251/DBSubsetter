//package e2e.fkreferencenonpk
//
//import e2e.AbstractSqlServerEndToEndTest
//
//class FkReferenceNonPkSqlServerTest extends AbstractSqlServerEndToEndTest with FkReferenceNonPkTestCases {
//  override val originPort = 5566
//  override val programArgs = Array(
//    "--schemas", "dbo",
//    "--baseQuery", "dbo.referenced_table ::: id in (1, 4) ::: includeChildren",
//    "--baseQuery", "dbo.referencing_table ::: id = 5 ::: includeChildren"
//  )
//}

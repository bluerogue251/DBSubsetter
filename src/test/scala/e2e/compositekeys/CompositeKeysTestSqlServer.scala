package e2e.compositekeys

import e2e.AbstractSqlServerEndToEndTest

class CompositeKeysTestSqlServer extends AbstractSqlServerEndToEndTest with CompositeKeysTest {

  override val programArgs = Array(
    "--schemas", "dbo",
  // Include the same parent in two different base queries
  "--baseQuery", "dbo.parents ::: id_one = 5 and id_two = 6 ::: includeChildren",
  "--baseQuery", "dbo.parents ::: id_one = 5 and id_two = 6 ::: includeChildren",
  // Include two different children with the same parent
  "--baseQuery", "dbo.children ::: id = 100 ::: excludeChildren",
  "--baseQuery", "dbo.children ::: id = 101 ::: excludeChildren"
  )
}

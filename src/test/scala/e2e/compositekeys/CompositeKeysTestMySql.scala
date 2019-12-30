package e2e.compositekeys

import e2e.AbstractMysqlEndToEndTest

class CompositeKeysTestMySql extends AbstractMysqlEndToEndTest with CompositeKeysTest {

  override val programArgs = Array(
    "--schemas", "composite_keys",
  // Include the same parent in two different base queries
  "--baseQuery", "composite_keys.parents ::: (id_one, id_two) = (5, 6) ::: includeChildren",
  "--baseQuery", "composite_keys.parents ::: id_one = 5 and id_two = 6 ::: includeChildren",
  // Include two different children with the same parent
  "--baseQuery", "composite_keys.children ::: id = 100 ::: excludeChildren",
  "--baseQuery", "composite_keys.children ::: id = 101 ::: excludeChildren"
  )
}

package e2e.compositekeys

import e2e.AbstractPostgresqlEndToEndTest

class CompositeKeysTestPostgreSQL extends AbstractPostgresqlEndToEndTest with CompositeKeysTest {

  override val programArgs = Array(
    "--schemas", "public",
    // Include the same parent in two different base queries
    "--baseQuery", "public.parents ::: (id_one, id_two) = (5, 6) ::: includeChildren",
    "--baseQuery", "public.parents ::: id_one = 5 and id_two = 6 ::: includeChildren",
    // Include two different children with the same parent
    "--baseQuery", "public.children ::: id = 100 ::: excludeChildren",
    "--baseQuery", "public.children ::: id = 101 ::: excludeChildren"
  )
}

package e2e.compositekeys

import e2e.PostgresSubsettingTest

class CompositeKeysTestPostgres extends PostgresSubsettingTest with CompositeKeysTest {
  // format: off

  override val programArgs = Array(
    "--schemas", "public",
    // Include the same parent in two different base queries
    "--baseQuery", "public.parents ::: (id_one, id_two) = (5, 6) ::: includeChildren",
    "--baseQuery", "public.parents ::: (id_one, id_two) = (5, 6) ::: includeChildren",
    // Include two different children with the same parent
    "--baseQuery", "public.children ::: id = 100 ::: excludeChildren",
    "--baseQuery", "public.children ::: id = 101 ::: excludeChildren"
  )
}

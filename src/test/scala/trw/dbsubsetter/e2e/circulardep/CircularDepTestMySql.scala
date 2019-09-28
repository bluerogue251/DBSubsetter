package trw.dbsubsetter.e2e.circulardep

import trw.dbsubsetter.e2e.AbstractMysqlEndToEndTest

class CircularDepTestMySql extends AbstractMysqlEndToEndTest with CircularDepTest {

  override val programArgs = Array(
    "--schemas", "circular_dep",
    "--baseQuery", "circular_dep.grandparents ::: id % 6 = 0 ::: includeChildren",
    "--skipPkStore", "circular_dep.children"
  )
}

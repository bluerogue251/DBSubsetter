package e2e.circulardep

import e2e.AbstractMysqlEndToEndTest

class CircularDepMysqlTest extends AbstractMysqlEndToEndTest with CircularDepTestCases {
  override protected val recreateOriginDB: Boolean = false

  override val originPort = 5480
  override val programArgs = Array(
    "--schemas", "circular_dep",
    "--baseQuery", "circular_dep.grandparents ::: id % 6 = 0 ::: includeChildren",
    "--skipPkStore", "circular_dep.children"
  )
}

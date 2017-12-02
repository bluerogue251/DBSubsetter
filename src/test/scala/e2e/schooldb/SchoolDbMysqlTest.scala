package e2e.schooldb

import org.scalatest.FunSuite

class SchoolDbMysqlTest extends FunSuite {
  // Pending until I rewrite the DML in vendor-agnostic slick
  // Also pending until I write a workaround to MySQL bug with wrong case of foreign key column
  test("MySQL SchoolDB Test") {
    pending
  }
}

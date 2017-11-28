package e2e

class MissingFkTest extends AbstractEndToEndTest {
  override val dataSetName = "missing_fk"
  override val originPort = 5490

  override val programArgs = Array(
    "--schemas", "missing_fk",
    "--baseQuery", "missing_fk.table_1 ::: id = 2 ::: true",
    "--foreignKey", "missing_fk.table_2(table_1_id) ::: missing_fk.table_1(id)",
    "--primaryKey", "missing_fk.table_4(table_1_id, table_3_id)"
  )

  test("Correct table_1 records were included") {
    assertCount("missing_fk", "table_1", None, 1)
    assertSum("missing_fk", "table_1", "id", 2)
  }

  test("Correct table_2 records were included") {
    // 1, 2
    assertCount("missing_fk", "table_2", None, 2)
    assertSum("missing_fk", "table_2", "id", 3)
  }

  test("Correct table_3 records were included") {
    // 45, 50
    assertCount("missing_fk", "table_3", None, 2)
    assertSum("missing_fk", "table_3", "id", 95)
  }

  test("Correct table_4 records were included") {
    // 2, 2
    assertCount("missing_fk", "table_4", None, 2)
    assertSum("missing_fk", "table_4", "table_1_id", 4)
    // 45, 50
    assertCount("missing_fk", "table_4", None, 2)
    assertSum("missing_fk", "table_4", "table_3_id", 95)
  }

  test("Correct table_5 records were included") {
    assertCount("missing_fk", "table_5", None, 1)
    assertSum("missing_fk", "table_5", "id", 99)
  }

  test("Correct table_a records were included") {
    pending
    val resultSet = targetSingleThreadedConn.createStatement().executeQuery("select * from table_a order by id asc")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 1)
    resultSet.next()
    val id2 = resultSet.getInt("id")
    assert(id2 === 2)
    resultSet.next()
    val id4 = resultSet.getInt("id")
    assert(id4 === 4)
    resultSet.next()
    val id5 = resultSet.getInt("id")
    assert(id5 === 5)
    assert(resultSet.next() === false)
  }

  test("Correct table_b records were included") {
    pending
    val resultSet = targetSingleThreadedConn.createStatement().executeQuery("select * from table_b")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 1)
    resultSet.next()
    val id2 = resultSet.getInt("id")
    assert(id2 === 2)
    assert(resultSet.next() === false)
  }

  test("Correct table_c records were included") {
    pending
    val resultSet = targetSingleThreadedConn.createStatement().executeQuery("select count(*) from table_c")
    resultSet.next()
    val count = resultSet.getInt(1)
    assert(count === 0)
  }

  test("Correct table_d records were included") {
    pending
    val resultSet = targetSingleThreadedConn.createStatement().executeQuery("select * from table_d order by id asc")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 2)
    assert(resultSet.next() === false)
  }
}

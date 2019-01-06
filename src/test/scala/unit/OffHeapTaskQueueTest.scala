package unit

import java.sql.JDBCType

import org.scalatest.FunSuite
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{Column, ForeignKey, SchemaInfo, Table}
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueueFactory
import trw.dbsubsetter.workflow.{FkTask, NewTasks}

/*
 * TODO add more test cases covering various combinations of:
 *   - DB Vendors
 *   - foreignKeyValue types (int, bigint, varchar, uuid, etc)
 *   - Single-col foreign keys, multi-col foreign keys
 *   - Schemas with just one table (self-referential foreign key)
 *   - Schemas with multiple foreign keys
 *   - etc.
 */
class OffHeapTaskQueueTest extends FunSuite {

  test("OffHeapTaskQueue returns an Option#None with no exception thrown when there is no data to read") {
    val config: Config = Config()
    val schemaInfo: SchemaInfo = OffHeapTaskQueueTest.schemaInfo
    val queue = OffHeapFkTaskQueueFactory.buildOffHeapFkTaskQueue(config, schemaInfo)
    // Dequeue several times -- it should always return `None` and should never throw an exception
    assert(queue.dequeue() === None)
    assert(queue.dequeue() === None)
    assert(queue.dequeue() === None)
  }

  test("OffHeapTaskQueue can succesfully write values and read them back (single column foreign key)") {
    val config: Config = Config()
    val schemaInfo: SchemaInfo = OffHeapTaskQueueTest.schemaInfo
    val queue = OffHeapFkTaskQueueFactory.buildOffHeapFkTaskQueue(config, schemaInfo)

    val fkValue1: Long = 7
    val fkValue2: Long = 10
    val fkValue3: Long = 23

    val rawTaskInfo: Map[(ForeignKey, Boolean), Array[Any]] =
      Map((OffHeapTaskQueueTest.foreignKey, false) -> Array[Any](fkValue1, fkValue2, fkValue3))

    queue.enqueue(NewTasks(rawTaskInfo))
    val baseTask: FkTask = FkTask(
      table = OffHeapTaskQueueTest.parentTable,
      fk = OffHeapTaskQueueTest.foreignKey,
      fkValue = "placeholder",
      fetchChildren = false
    )

    // Dequeue the three FkTasks
    assert(queue.dequeue() === Some(baseTask.copy(fkValue = 7)))
    assert(queue.dequeue() === Some(baseTask.copy(fkValue = 10)))
    assert(queue.dequeue() === Some(baseTask.copy(fkValue = 23)))
    // All the data has been drained from the queue -- now we get `None`
    assert(queue.dequeue() === None)
  }
}

/*
 * The large amount of static data needed to set this unit test up (data a lot of which is not even
 * directly used by the unit test itself) shows that our implementation should eventually be cleaned up
 */
private[this] object OffHeapTaskQueueTest {

  private val parentTable: Table =
    Table("public", "parent", hasSqlServerAutoIncrement = false, storePks = true)

  private[this] val parentPkColumn: Column =
    Column(
      table = parentTable,
      name = "id",
      ordinalPosition = 4,
      jdbcType = JDBCType.BIGINT,
      typeName = "whatever"
    )

  private[this] val childTable:
    Table = Table("public", "child", hasSqlServerAutoIncrement = false, storePks = true)

  private[this] val childFkColumn: Column =
    Column(
      table = childTable,
      name = "parentId",
      ordinalPosition = 7,
      jdbcType = JDBCType.BIGINT,
      typeName = "whatever"
    )

  private val foreignKey: ForeignKey = ForeignKey(
    fromCols = Vector(childFkColumn),
    toCols = Vector(parentPkColumn),
    pointsToPk = true,
    i = 0
  )

  private val schemaInfo: SchemaInfo = {
    SchemaInfo(Map.empty, Map.empty, Map.empty, Array(foreignKey), Map.empty, Map.empty, null)
  }
}

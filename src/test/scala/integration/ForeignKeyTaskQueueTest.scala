package integration

import org.scalatest.FunSuite
import trw.dbsubsetter.db.{Column, ColumnTypes, ForeignKey, ForeignKeyValue, Schema, SchemaInfo, Table}
import trw.dbsubsetter.fkcalc.{FetchParentTask, ForeignKeyTask}
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue

import java.nio.file.{Files, Path}

/*
 * TODO add more test cases covering various combinations of:
 *   - DB Vendors
 *   - foreignKeyValue types (int, bigint, varchar, uuid, etc)
 *   - Single-col foreign keys, multi-col foreign keys
 *   - Schemas with just one table (self-referential foreign key)
 *   - Schemas with multiple foreign keys
 *   - etc.
 */
class ForeignKeyTaskQueueTest extends FunSuite {

  test("OffHeapTaskQueue returns an Option#None with no exception thrown when there is no data to read") {
    val schemaInfo: SchemaInfo = ForeignKeyTaskQueueTest.schemaInfo
    val queue = ForeignKeyTaskQueue.from(createStorageDir(), schemaInfo)
    // Dequeue several times -- it should always return `None` and should never throw an exception
    assert(queue.dequeue() === None)
    assert(queue.dequeue() === None)
    assert(queue.dequeue() === None)
  }

  test("OffHeapTaskQueue can succesfully write values and read them back (single column foreign key)") {
    val schemaInfo: SchemaInfo = ForeignKeyTaskQueueTest.schemaInfo
    val queue = ForeignKeyTaskQueue.from(createStorageDir(), schemaInfo)

    val fkValue1: ForeignKeyValue = new ForeignKeyValue(Seq[Long](7))
    val fkValue2: ForeignKeyValue = new ForeignKeyValue(Seq[Long](10))
    val fkValue3: ForeignKeyValue = new ForeignKeyValue(Seq[Long](23))

    val fk: ForeignKey = ForeignKeyTaskQueueTest.foreignKey

    val task1: ForeignKeyTask = FetchParentTask(fk, fkValue1)
    val task2: ForeignKeyTask = FetchParentTask(fk, fkValue2)
    val task3: ForeignKeyTask = FetchParentTask(fk, fkValue3)
    queue.enqueue(task1)
    queue.enqueue(task2)
    queue.enqueue(task3)

    // Dequeue the three FkTasks
    val firstTask: FetchParentTask = queue.dequeue().get.asInstanceOf[FetchParentTask]
    val secondTask: FetchParentTask = queue.dequeue().get.asInstanceOf[FetchParentTask]
    val thirdTask: FetchParentTask = queue.dequeue().get.asInstanceOf[FetchParentTask]
    // All the data has been drained from the queue -- now we get `None`
    assert(queue.dequeue() === None)

    Seq(firstTask, secondTask, thirdTask).foreach { dequeuedTask =>
      assert(dequeuedTask.fk === ForeignKeyTaskQueueTest.foreignKey)
      assert(dequeuedTask.fk.toTable === ForeignKeyTaskQueueTest.parentTable)
    }

    assert(firstTask.fkValueFromChild.individualColumnValues === fkValue1.individualColumnValues)
    assert(secondTask.fkValueFromChild.individualColumnValues === fkValue2.individualColumnValues)
    assert(thirdTask.fkValueFromChild.individualColumnValues === fkValue3.individualColumnValues)
  }

  private[this] def createStorageDir(): Path = {
    Files.createTempDirectory("ForeignKeyTaskQueueTest-")
  }
}

/*
 * The large amount of static data needed to set this unit test up (data a lot of which is not even
 * directly used by the unit test itself) shows that our implementation should eventually be cleaned up
 */
private[this] object ForeignKeyTaskQueueTest {

  private val parentTable: Table =
    Table(
      schema = Schema("public"),
      name = "parent"
    )

  private[this] val parentPkColumn: Column =
    new Column(
      table = parentTable,
      name = "id",
      ColumnTypes.Long
    )

  private[this] val childTable: Table =
    Table(
      schema = Schema("public"),
      name = "child"
    )

  private[this] val childFkColumn: Column =
    new Column(
      table = childTable,
      name = "parentId",
      ColumnTypes.Long
    )

  private val foreignKey: ForeignKey =
    new ForeignKey(
      fromCols = Vector(childFkColumn),
      toCols = Vector(parentPkColumn),
      pointsToPk = true
    )

  private val schemaInfo: SchemaInfo =
    new SchemaInfo(
      tables = Seq.empty,
      keyColumnsByTable = Map.empty,
      dataColumnsByTable = Map.empty,
      pksByTable = Map.empty,
      foreignKeys = Array(foreignKey),
      fksFromTable = Map.empty,
      fksToTable = Map.empty
    )
}

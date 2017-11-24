package e2e

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.Application
import trw.dbsubsetter.db.{ColumnName, SchemaName, TableName}

import scala.sys.process._


abstract class AbstractEndToEndTest extends FunSuite with BeforeAndAfterAll {
  def dataSetName: String

  def originPort: Int

  def targetPort: Int

  def programArgs: Array[String]

  var targetConn: Connection = _

  def originDbName = s"${dataSetName}_origin"

  def targetDbName = s"${dataSetName}_target"

  def originConnString = s"jdbc:postgresql://localhost:$originPort/$originDbName?user=postgres"

  def targetConnString = s"jdbc:postgresql://localhost:$targetPort/$targetDbName?user=postgres"

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    s"./util/reset_origin_db.sh $dataSetName $originDbName $originPort".!!
    s"./util/reset_target_db.sh $originDbName $originPort $targetDbName $targetPort".!!

    Application.main(programArgs)

    s"./util/post_subset_target.sh $originDbName $originPort $targetDbName $targetPort".!!

    targetConn = DriverManager.getConnection(targetConnString)
    targetConn.setReadOnly(true)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    targetConn.close()
  }

  protected def countTable(schema: SchemaName, table: TableName): Long = {
    val resultSet = targetConn.createStatement().executeQuery(s"""select count(*) from "$schema"."$table"""")
    resultSet.next()
    resultSet.getLong(1)
  }

  protected def sumColumn(schema: SchemaName, table: TableName, column: ColumnName): Long = {
    val resultSet = targetConn.createStatement().executeQuery(s"""select sum("$column") from "$schema"."$table"""")
    resultSet.next()
    resultSet.getLong(1)
  }
}

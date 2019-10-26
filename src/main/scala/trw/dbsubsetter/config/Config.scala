package trw.dbsubsetter.config

import java.io.File

import trw.dbsubsetter.db.{ColumnName, SchemaName, TableName, WhereClause}

case class Config(
  schemas: Seq[String] = Seq.empty,
  originDbConnectionString: String = "",
  targetDbConnectionString: String = "",
  baseQueries: Vector[((SchemaName, TableName), WhereClause, Boolean)] = Vector.empty,
  // TODO rework these to be "Key query database connections" and "Data copy database connections"
  originDbParallelism: Int = 1,
  targetDbParallelism: Int = 1,
  cmdLineForeignKeys: List[CmdLineForeignKey] = List.empty,
  cmdLinePrimaryKeys: List[CmdLinePrimaryKey] = List.empty,
  excludeColumns: Map[(SchemaName, TableName), Set[ColumnName]] = Map.empty.withDefaultValue(Set.empty),
  excludeTables: Set[(SchemaName, TableName)] = Set.empty,
  preTargetBufferSize: Int = 100,
  taskQueueDirOpt: Option[File] = None,
  isSingleThreadedDebugMode: Boolean = false,
  exposeMetrics: Boolean = false
)

package trw.dbsubsetter.config

import trw.dbsubsetter.db.{ColumnName, SchemaName, TableName, WhereClause}

case class Config(schemas: Seq[String] = Seq.empty,
                  originDbConnectionString: String = "",
                  targetDbConnectionString: String = "",
                  baseQueries: List[((SchemaName, TableName), WhereClause, Boolean)] = List.empty,
                  originDbParallelism: Int = 1,
                  targetDbParallelism: Int = 1,
                  cmdLineForeignKeys: List[CmdLineForeignKey] = List.empty,
                  cmdLinePrimaryKeys: List[CmdLinePrimaryKey] = List.empty,
                  excludeColumns: Map[(SchemaName, TableName), Set[ColumnName]] = Map.empty.withDefaultValue(Set.empty),
                  excludeTables: Set[(SchemaName, TableName)] = Set.empty,
                  skipPkStore: Set[(SchemaName, TableName)] = Set.empty,
                  isSingleThreadedDebugMode: Boolean = false)

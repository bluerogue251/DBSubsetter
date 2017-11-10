package trw.dbsubsetter.config

import trw.dbsubsetter.db.{SchemaName, TableName, WhereClause}

case class Config(schemas: Seq[String] = Seq.empty,
                  originDbConnectionString: String = "",
                  targetDbConnectionString: String = "",
                  baseQueries: Map[(SchemaName, TableName), WhereClause] = Map.empty,
                  dbParallelism: Int = 20)

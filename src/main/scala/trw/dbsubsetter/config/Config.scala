package trw.dbsubsetter.config

import trw.dbsubsetter.db.{SchemaName, TableName, WhereClause}

case class Config(schemas: Seq[String] = Seq.empty,
                  originDbConnectionString: String = "",
                  targetDbConnectionString: String = "",
                  baseQueries: List[((SchemaName, TableName), WhereClause)] = List.empty,
                  cmdLineStandardFks: List[CommandLineForeignKey] = List.empty,
                  originDbParallelism: Int = 1,
                  targetDbParallelism: Int = 1)

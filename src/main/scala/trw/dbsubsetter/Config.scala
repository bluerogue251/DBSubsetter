package trw.dbsubsetter

case class Config(schemas: Seq[String] = Seq.empty,
                  originDbConnectionString: String = "",
                  targetDbConnectionString: String = "",
                  baseQueries: Map[(SchemaName, TableName), WhereClause] = Map.empty)

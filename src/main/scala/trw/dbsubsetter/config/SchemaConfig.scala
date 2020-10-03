package trw.dbsubsetter.config

import trw.dbsubsetter.db.{Schema, Table}

case class SchemaConfig(
    schemas: Set[Schema] = Set.empty,
    baseQueries: Set[ConfigBaseQuery] = Set.empty,
    extraPrimaryKeys: Set[ConfigPrimaryKey] = Set.empty,
    extraForeignKeys: Set[ConfigForeignKey] = Set.empty,
    excludeTables: Set[Table] = Set.empty,
    excludeColumns: Set[ConfigColumn] = Set.empty
)

case class ConfigBaseQuery(
    table: Table,
    whereClause: String,
    includeChildren: Boolean
)

case class ConfigForeignKey(
    fromTable: Table,
    fromColumns: Seq[ConfigColumn],
    toTable: Table,
    toColumns: Seq[ConfigColumn]
)

case class ConfigPrimaryKey(
    table: Table,
    columns: Seq[ConfigColumn]
)

case class ConfigColumn(
    table: Table,
    name: String
)

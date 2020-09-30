package trw.dbsubsetter.config

import trw.dbsubsetter.db.{Schema, Table}

/**
  * @param schemas
  * @param baseQueries
  * @param extraForeignKeys
  * @param extraPrimaryKeys
  * @param excludeTables
  * @param excludeColumns
  */
case class SchemaConfig(
    schemas: Seq[Schema] = Seq.empty,
    baseQueries: Seq[CmdLineBaseQuery] = Seq.empty,
    extraForeignKeys: Set[CmdLineForeignKey] = Set.empty,
    extraPrimaryKeys: Set[CmdLinePrimaryKey] = Set.empty,
    excludeTables: Set[Table] = Set.empty,
    excludeColumns: Set[CmdLineColumn] = Set.empty
)

package trw.dbsubsetter.config

import trw.dbsubsetter.db.{ColumnName, Schema, Table}


case class SchemaConfig(
  schemas: Seq[Schema] = Seq.empty,
  extraForeignKeys: Seq[CmdLineForeignKey] = Seq.empty,
  extraPrimaryKeys: Seq[CmdLinePrimaryKey] = Seq.empty,
  excludeColumns: Map[Table, Set[ColumnName]] = Map.empty.withDefaultValue(Set.empty),
  excludeTables: Set[Table] = Set.empty,
)

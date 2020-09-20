package trw.dbsubsetter.config

import java.io.File

import trw.dbsubsetter.db.{ColumnName, Schema, Table}


case class Config(
    schemas: Seq[Schema] = Seq.empty,
    originDbConnectionString: String = "",
    targetDbConnectionString: String = "",
    baseQueries: Seq[CmdLineBaseQuery] = Seq.empty,
    keyCalculationDbConnectionCount: Int = 1,
    dataCopyDbConnectionCount: Int = 1,
    extraForeignKeys: Seq[CmdLineForeignKey] = Seq.empty,
    extraPrimaryKeys: Seq[CmdLinePrimaryKey] = Seq.empty,
    excludeColumns: Map[Table, Set[ColumnName]] = Map.empty.withDefaultValue(Set.empty),
    excludeTables: Set[Table] = Set.empty,
    tempfileStorageDirectoryOverride: Option[File] = None,
    singleThreadDebugMode: Boolean = false,
    exposeMetrics: Boolean = false
)

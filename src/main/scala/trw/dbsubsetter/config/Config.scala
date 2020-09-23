package trw.dbsubsetter.config

import java.io.File

import trw.dbsubsetter.db.{Schema, Table}

case class Config(
    schemas: Seq[Schema] = Seq.empty,
    originDbConnectionString: String = "",
    targetDbConnectionString: String = "",
    baseQueries: Seq[CmdLineBaseQuery] = Seq.empty,
    keyCalculationDbConnectionCount: Int = 1,
    dataCopyDbConnectionCount: Int = 1,
    extraForeignKeys: Set[CmdLineForeignKey] = Set.empty,
    extraPrimaryKeys: Set[CmdLinePrimaryKey] = Set.empty,
    excludeTables: Set[Table] = Set.empty,
    excludeColumns: Set[CmdLineColumn] = Set.empty,
    tempfileStorageDirectoryOverride: Option[File] = None,
    singleThreadDebugMode: Boolean = false,
    exposeMetrics: Boolean = false
)

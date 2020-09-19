package trw.dbsubsetter.config

import java.io.File

import trw.dbsubsetter.db.{ColumnName, Table}

case class Config(
    schemas: Seq[String] = Seq.empty,
    originDbConnectionString: String = "",
    targetDbConnectionString: String = "",
    baseQueries: Seq[CmdLineBaseQuery] = Seq.empty,
    keyCalculationDbConnectionCount: Int = 1,
    dataCopyDbConnectionCount: Int = 1,
    cmdLineForeignKeys: Seq[CmdLineForeignKey] = Seq.empty,
    cmdLinePrimaryKeys: Seq[CmdLinePrimaryKey] = Seq.empty,
    excludeColumns: Map[Table, Set[ColumnName]] = Map.empty.withDefaultValue(Set.empty),
    excludeTables: Set[Table] = Set.empty,
    tempfileStorageDirectoryOpt: Option[File] = None,
    isSingleThreadedDebugMode: Boolean = false,
    exposeMetrics: Boolean = false
)

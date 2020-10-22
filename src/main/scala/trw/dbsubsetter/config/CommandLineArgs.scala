package trw.dbsubsetter.config

import java.nio.file.Path

case class CommandLineArgs(
    schemas: Set[String] = Set.empty,
    originDbConnectionString: String = "",
    targetDbConnectionString: String = "",
    baseQueries: Set[String] = Set.empty,
    keyCalculationDbConnectionCount: Int = 1,
    dataCopyDbConnectionCount: Int = 1,
    extraForeignKeys: Set[String] = Set.empty,
    extraPrimaryKeys: Set[String] = Set.empty,
    excludeTables: Set[String] = Set.empty,
    excludeColumns: Set[String] = Set.empty,
    storageDirectory: Option[Path] = None,
    metricsPort: Option[Int] = None
)

package trw.dbsubsetter.config

import java.io.File

import scopt.OptionParser
import trw.dbsubsetter.db.Schema
import trw.dbsubsetter.db.Table

object CommandLineParser {
  val parser: OptionParser[Config] = new OptionParser[Config]("DBSubsetter") {
    head("DBSubsetter", "v1.0.0-beta.4")
    help("help").text("Prints this usage text\n")
    version("version").text("Prints the application version\n")

    opt[Seq[String]]("schemas")
      .valueName("<schema1>,<schema2>,<schema3>, ...")
      .action((schemaNames, c) => c.copy(schemas = schemaNames.map(schemaName => Schema(schemaName.trim))))
      .required()
      .text("Names of the schemas to include when subsetting\n")

    opt[String]("originDbConnStr")
      .valueName("<jdbc connection string>")
      .required()
      .action((cs, c) => c.copy(originDbConnectionString = cs.trim))
      .text("""JDBC connection string to the full-size origin db, for example:
              |                           MySQL:       jdbc:mysql://<originHost>:<originPort>/<originDb>?user=<originUser>&password=<originPassword>&rewriteBatchedStatements=true
              |                           PostgreSQL:  jdbc:postgresql://<originHost>:<originPort>/<originDb>?user=<originUser>&password=<originPassword>
              |                           SQL Server:  jdbc:sqlserver://<originHost>:<originPort>;databaseName=<originDb>;user=<originUser>;password=<originPassword>
              |""".stripMargin)

    opt[String]("targetDbConnStr")
      .valueName("<jdbc connection string>")
      .required()
      .action((cs, c) => c.copy(targetDbConnectionString = cs.trim))
      .text("""JDBC connection string to the smaller target db, for example:
              |                           MySQL:       jdbc:mysql://<targetHost>:<targetPort>/<targetDb>?user=<targetUser>&password=<targetPassword>&rewriteBatchedStatements=true
              |                           PostgreSQL:  jdbc:postgresql://<targetHost>:<targetPort>/<targetDb>?user=<targetUser>&password=<targetPassword>
              |                           SQL Server:  jdbc:sqlserver://<targetHost>:<targetPort>;databaseName=<targetDb>;user=<targetUser>;password=<targetPassword>
              |""".stripMargin)

    opt[String]("baseQuery")
      .required()
      .maxOccurs(Int.MaxValue)
      .valueName("<schema>.<table> ::: <whereClause> ::: <includeChildren|excludeChildren>")
      .action { case (bq, c) =>
        val r = """^\s*(.+)\.(.+)\s+:::\s+(.+)\s+:::\s+(includeChildren|excludeChildren)\s*$""".r
        bq match {
          case r(schemaName, tableName, whereClause, includeChildren) =>
            val table = normalizeTable(schemaName, tableName)
            val baseQuery = CmdLineBaseQuery(table, whereClause.trim, includeChildren == "includeChildren")
            c.copy(baseQueries = c.baseQueries :+ baseQuery)
          case _ => throw new RuntimeException()
        }
      }
      .text("""Starting table, where-clause, and includeChildren/excludeChildren to kick off subsetting
              |                           includeChildren is recommended for most use cases
              |                              It continues downwards recursively, meaning children of the children are also fetched, etc
              |                              It does *not* continue upwards, meaning children of *parents* will *not* be fetched
              |                              Not continuing upwards is important for keeping the resulting dataset small
              |                           excludeChildren is mostly for edge cases such as ensuring an entire table is kept:
              |                              --baseQuery "public.invoice_types ::: true ::: excludeChildren"
              |                              would includes the entire invoice_types table but would not fetch any of its children.
              |                              This is often useful for tables containing static "domain data".
              |                           Can be specified multiple times
              |""".stripMargin)

    opt[Int]("keyCalculationDbConnectionCount")
      .valueName("<int>")
      .action((dbp, c) => c.copy(keyCalculationDbConnectionCount = dbp))
      .text("""Concurrent connections to the Origin DB for calculating primary and foreign key dependencies
              |                           A reasonable starting value is half the number of CPU cores on your origin database machine
              |                           (Note the total Origin DB connection count is --keyCalculationDbConnectionCount + --dataCopyDbConnectionCount)
        """.stripMargin)

    opt[Int]("dataCopyDbConnectionCount")
      .valueName("<int>")
      .action((dbp, c) => c.copy(dataCopyDbConnectionCount = dbp))
      .text("""Concurrent connections to both the Origin DB and the Target DB for copying over full row data
              |                           A reasonable starting value is half the number of CPU cores on your target database machine
              |                           (Note the total Origin DB connection count is --keyCalculationDbConnectionCount + --dataCopyDbConnectionCount)
        """.stripMargin)

    opt[String]("foreignKey")
      .maxOccurs(Int.MaxValue)
      .valueName("<schema1>.<table1>(<column1>, <column2>, ...) ::: <schema2>.<table2>(<column3>, <column4>, ...)")
      .action { case (fk, c) =>
        val regex = """^(.+)\.(.+)\((.+)\)\s+:::\s+(.+)\.(.+)\((.+)\)\s*$""".r

        fk match {
          case regex(fromSchemaName, fromTableName, fromCols, toSchemaName, toTableName, toCols) =>
            val fromTable = normalizeTable(fromSchemaName, fromTableName)
            val fromColumns = normalizeColumns(fromTable, fromCols)
            val toTable = normalizeTable(toSchemaName, toTableName)
            val toColumns = normalizeColumns(toTable, toCols)
            val cmdLineForeignKey = CmdLineForeignKey(fromTable, fromColumns, toTable, toColumns)
            c.copy(extraForeignKeys = c.extraForeignKeys + cmdLineForeignKey)
          case _ => throw new RuntimeException()
        }
      }
      .text("""Foreign key to respect during subsetting even though it is not defined in the database
              |                           Can be specified multiple times
              |""".stripMargin)

    opt[String]("primaryKey")
      .maxOccurs(Int.MaxValue)
      .valueName("<schema>.<table>(<column1>, <column2>, ...)")
      .action { case (fk, c) =>
        val regex = """^\s*(.+)\.(.+)\((.+)\)\s*$""".r
        fk match {
          case regex(schemaName, tableName, cols) =>
            val table = normalizeTable(schemaName, tableName)
            val columns = normalizeColumns(table, cols)
            val cmdLinePrimaryKey = CmdLinePrimaryKey(table, columns)
            c.copy(extraPrimaryKeys = c.extraPrimaryKeys + cmdLinePrimaryKey)
          case _ => throw new RuntimeException()
        }
      }
      .text("""Primary key to recognize during subsetting even though it is not defined in the database
              |                           Can be specified multiple times
              |""".stripMargin)

    opt[String]("excludeTable")
      .valueName("<schema>.<table>")
      .maxOccurs(Int.MaxValue)
      .action { (str, c) =>
        val regex = """^\s*(.+)\.(.+)\s*$""".r
        str match {
          case regex(schemaName, tableName) =>
            val table = normalizeTable(schemaName, tableName)
            c.copy(excludeTables = c.excludeTables + table)
          case _ => throw new RuntimeException
        }
      }
      .text("""Exclude a table from the resulting subset
              |                           Also ignore all foreign keys to and from this table
              |                           Can be specified multiple times
              |""".stripMargin)

    opt[String]("excludeColumns")
      .valueName("<schema>.<table>(<column1>, <column2>, ...)")
      .maxOccurs(Int.MaxValue)
      .action { (ic, c) =>
        val regex = """^\s*(.+)\.(.+)\((.+)\)\s*$""".r
        ic match {
          case regex(schemaName, tableName, cols) =>
            val table = normalizeTable(schemaName, tableName)
            val newlyExcluded = normalizeColumns(table, cols).toSet
            c.copy(excludeColumns = c.excludeColumns ++ newlyExcluded)
          case _ => throw new RuntimeException
        }
      }
      .text("""Exclude data from these columns when subsetting
              |                           Intended for columns that are not part of any primary or foreign keys
              |                           Useful among other things as a workaround if DBSubsetter does not support a vendor-specific data type
              |                           Can be specified multiple times
              |""".stripMargin)

    opt[File]("tempfileStorageDirectory")
      .valueName("</path/to/tempfile/storage/directory>")
      .action((dir, c) => c.copy(tempfileStorageDirectoryOverride = Some(dir)))
      .validate { dir =>
        if (!dir.exists()) dir.mkdir()
        if (!dir.isDirectory) {
          failure("--tempfileStorageDirectory must be a directory")
        } else if (dir.listFiles().nonEmpty) {
          failure("--tempfileStorageDirectory must be an empty directory")
        } else {
          success
        }
      }
      .text("""Directory in which DBSubsetter will store tempfiles containing intermediate results
              |                           Defaults to the standard tempfile location of your OS
              |""".stripMargin)

    opt[Unit]("singleThreadedDebugMode")
      .action((_, c) => c.copy(singleThreadDebugMode = true))
      .text("""Run DBSubsetter in debug mode (NOT recommended)
              |                           Uses a simplified, single-threaded architecture
              |                           Avoids using Akka Streams and Chronicle-Queue
              |                           Ignores `--keyCalculationDbConnectionCount` and `--dataCopyDbConnectionCount` and uses one connection per database
              |                           Subsetting may be significantly slower
              |                           The resulting subset should be exactly the same as in regular mode
              |""".stripMargin)

    opt[Unit]("exposeMetrics")
      .action((_, c) => c.copy(exposeMetrics = true))
      .text("""Exposes performance metrics at localhost:9092/metrics
              |                           (Mainly for debugging purposes)
              |                           Designed for use with Prometheus (https://prometheus.io/) and Grafana (https://grafana.com/)
              |                           See the `observability-tools.sh` shell script in the project root for an example of how to visualize these metrics
              |""".stripMargin)

    private val usageExamples =
      """
        |Examples:
        |
        |   # Simple configuration:
        |      java -jar /path/to/DBSubsetter.jar \
        |        --schemas public \
        |        --originDbConnStr "jdbc:postgresql://localhost:5450/origin_db_name?user=yourUser&password=yourPassword" \
        |        --targetDbConnStr "jdbc:postgresql://localhost:5451/target_db_name?user=yourUser&password=yourPassword" \
        |        --baseQuery "public.users ::: id % 100 = 0 ::: includeChildren" \
        |        --keyCalculationDbConnectionCount 10 \
        |        --dataCopyDbConnectionCount 10
        |
        |
        |   # Multiple starting conditions (a.k.a. multiple "base queries"):
        |      java -jar /path/to/DBSubsetter.jar \
        |        --schemas "public, audit, finance" \
        |        --originDbConnStr "jdbc:postgresql://localhost:5450/origin_db_name?user=yourUser&password=yourPassword" \
        |        --targetDbConnStr "jdbc:postgresql://localhost:5451/target_db_name?user=yourUser&password=yourPassword" \
        |        --baseQuery "public.students ::: student_id in (select v.student_id from valedictorians as v where v.year = 2017) ::: includeChildren", \
        |        --baseQuery "public.users ::: random() < 0.001 ::: includeChildren", \
        |        --baseQuery "finance.transactions ::: created_at < '2017-12-25' ::: excludeChildren" \
        |        --keyCalculationDbConnectionCount 15 \
        |        --dataCopyDbConnectionCount 20
        |
        |
        |   # Specifying missing foreign and primary keys at the command line (keys can have one or more columns):
        |      java -jar /path/to/DBSubsetter.jar \
        |        --schemas AdventureWorksSchema,HistorySchema \
        |        --originDbConnStr "jdbc:sqlserver://db-1.example.com:1433;databaseName=myCorpDb;user=sa;password=saPassword" \
        |        --targetDbConnStr "jdbc:sqlserver://db-2.example.com:1433;databaseName=myCorpDb;user=sa;password=saPassword" \
        |        --baseQuery "AdventureWorksSchema.users ::: id in (3, 4, 5, 6, 7) ::: excludeChildren", \
        |        --foreignKey "AdventureWorksSchema.users(departmentId) ::: AdventureWorksSchema.departments(Id))" \
        |        --foreignKey "HistorySchema.EventLogTable(userId, employeeType) ::: AdventureWorksSchema.users(Id, employeeType))" \
        |        --primaryKey "HistorySchema.EventLogTable(Id)" \
        |        --primaryKey "AdventureWorksSchema.UsersRolesJoinTable(UserId, RoleId)" \
        |        --keyCalculationDbConnectionCount 1 \
        |        --dataCopyDbConnectionCount 1
        |
        |
        |   # Allowing DBSubsetter to use up to 8 gigabytes of memory:
        |      java -Xmx8G -jar /path/to/DBSubsetter.jar [...]
        |
        |Notes:
        |
        |   # Arguments containing whitespace, parentheses, and other special characters must be enclosed in quotes:
        |      (OK)    --schemas public,audit,finance
        |      (OK)    --schemas "public, audit, finance"
        |      (OK)    --schemas 'public, audit, finance'
        |      (OK)    --schemas "public","audit","finance"
        |      (ERROR) --schemas public, audit, finance
        |      (ERROR) --schemas "public", "audit", "finance"
        |
        |      (OK)    --excludeColumns "dboSchema.myTable(myColumn)"
        |      (OK)    --excludeColumns 'dboSchema.myTable(myColumn)'
        |      (ERROR) --excludeColumns dboSchema.myTable(myColumn)
        |
        |   # Arguments containing a quotation mark must either alternate single and double quotes or use backslash escaping:
        |      (OK)    --baseQuery 'primary_schools."Districts" ::: "Districts"."Id" in (2, 78, 945) ::: includeChildren'
        |      (OK)    --baseQuery "primary_schools.\"Districts\" ::: \"Districts\".\"Id\" in (2, 78, 945) ::: includeChildren"
        |      (ERROR) --baseQuery "primary_schools."Districts" ::: "Districts"."Id" in (2, 78, 945) ::: includeChildren"
        |""".stripMargin
    note(usageExamples)
  }

  private def normalizeTable(schemaName: String, tableName: String): Table = {
    val schema = Schema(schemaName.trim)
    Table(schema = schema, name = tableName.trim)
  }

  private def normalizeColumns(table: Table, untrimmedColumnCsvs: String): Seq[CmdLineColumn] = {
    untrimmedColumnCsvs
      .split(",")
      .map(_.trim)
      .map(columnName => CmdLineColumn(table, columnName))
  }
}

case class CmdLineBaseQuery(
    table: Table,
    whereClause: String,
    includeChildren: Boolean
)

case class CmdLineForeignKey(
    fromTable: Table,
    fromColumns: Seq[CmdLineColumn],
    toTable: Table,
    toColumns: Seq[CmdLineColumn]
)

case class CmdLinePrimaryKey(
    table: Table,
    columns: Seq[CmdLineColumn]
)

case class CmdLineColumn(
    table: Table,
    name: String
)

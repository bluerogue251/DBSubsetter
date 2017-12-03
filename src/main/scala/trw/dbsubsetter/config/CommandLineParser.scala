package trw.dbsubsetter.config

import scopt.OptionParser
import trw.dbsubsetter.db.{ColumnName, SchemaName, TableName}

object CommandLineParser {
  val parser: OptionParser[Config] = new OptionParser[Config]("DBSubsetter") {
    head("DBSubsetter", "v1.0.0-beta.1")
    help("help").text("Prints this usage text\n")
    version("version").text("Prints the application version\n")

    opt[Seq[String]]("schemas")
      .valueName("<schema1>, <schema2>, <schema3>, ...")
      .action((s, c) => c.copy(schemas = s.map(_.trim)))
      .required()
      .text("Names of the schemas to include when subsetting\n")

    opt[String]("originDbConnStr")
      .valueName("<jdbc connection string>")
      .required()
      .action((cs, c) => c.copy(originDbConnectionString = cs.trim))
      .text(
        """JDBC connection string to the full-size origin db, for example:
          |                           MySQL:       jdbc:mysql://<originHost>:<originPort>/<originDb>?user=<originUser>&password=<originPassword>&rewriteBatchedStatements=true
          |                           PostgreSQL:  jdbc:postgresql://<originHost>:<originPort>/<originDb>?user=<originUser>&password=<originPassword>
          |                           SQL Server:  jdbc:sqlserver://<originHost>:<originPort>;databaseName=<originDb>;user=<originUser>;password=<originPassword>
          |""".stripMargin)

    opt[String]("targetDbConnStr")
      .valueName("<jdbc connection string>")
      .required()
      .action((cs, c) => c.copy(targetDbConnectionString = cs.trim))
      .text(
        """JDBC connection string to the smaller target db, for example:
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
          case r(schema, table, whereClause, fetchChildren) =>
            val fc = fetchChildren == "includeChildren"
            c.copy(baseQueries = ((schema.trim, table.trim), whereClause.trim, fc) :: c.baseQueries)
          case _ => throw new RuntimeException()
        }
      }
      .text(
        """Starting table, where-clause, and includeChildren/excludeChildren to kick off subsetting
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

    opt[Int]("originDbParallelism")
      .valueName("<int>")
      .required()
      .action((dbp, c) => c.copy(originDbParallelism = dbp))
      .text("Number of concurrent connections to the full-size origin DB\n")

    opt[Int]("targetDbParallelism")
      .valueName("<int>")
      .required()
      .action((dbp, c) => c.copy(targetDbParallelism = dbp))
      .text("Number of concurrent connections to the smaller target DB\n")

    opt[String]("foreignKey")
      .maxOccurs(Int.MaxValue)
      .valueName("<schema1>.<table1>(<column1>, <column2>, ...) ::: <schema2>.<table2>(<column3>, <column4>, ...)")
      .action { case (fk, c) =>
        val regex = """^(.+)\.(.+)\((.+)\)\s+:::\s+(.+)\.(.+)\((.+)\)\s*$""".r

        fk match {
          case regex(fromSch, fromTbl, fromCols, toSch, toTbl, toCols) =>
            val fk = CmdLineForeignKey(fromSch.trim, fromTbl.trim, fromCols.split(",").toList.map(_.trim), toSch.trim, toTbl.trim, toCols.split(",").toList.map(_.trim))
            c.copy(cmdLineForeignKeys = fk :: c.cmdLineForeignKeys)
          case _ => throw new RuntimeException()
        }
      }
      .text(
        """Foreign key to respect during subsetting even though it is not defined in the database
          |                           Can be specified multiple times
          |""".stripMargin)

    opt[String]("primaryKey")
      .maxOccurs(Int.MaxValue)
      .valueName("<schema1>.<table1>(<column1>, <column2>, ...)")
      .action { case (fk, c) =>
        val regex = """^\s*(.+)\.(.+)\((.+)\)\s*$""".r
        fk match {
          case regex(sch, tbl, cols) =>
            val pk = CmdLinePrimaryKey(sch.trim, tbl.trim, cols.split(",").toList.map(_.trim))
            c.copy(cmdLinePrimaryKeys = pk :: c.cmdLinePrimaryKeys)
          case _ => throw new RuntimeException()
        }
      }
      .text(
        """Primary key to recognize during subsetting even though it is not defined in the database
          |                           Can be specified multiple times
          |""".stripMargin)

    opt[String]("excludeColumns")
      .valueName("<schema>.<table>(<column1>, <column2>, ...)")
      .maxOccurs(Int.MaxValue)
      .action { (ic, c) =>
        val regex = """^\s*(.+)\.(.+)\((.+)\)\s*$""".r
        ic match {
          case regex(schema, table, columnListString) =>
            val alreadyExcluded = c.excludeColumns((schema.trim, table.trim))
            val newlyExcluded = columnListString.split(",").map(_.trim).toSet
            c.copy(excludeColumns = c.excludeColumns.updated((schema.trim, table.trim), alreadyExcluded ++ newlyExcluded))
          case _ => throw new RuntimeException

        }
      }
      .text(
        """Exclude data from these columns when subsetting
          |                           Intended for columns that are not part of any primary or foreign keys
          |                           Useful among other things as a workaround if DBSubsetter does not support a vendor-specific data type
          |                           Can be specified multiple times
          |""".stripMargin)

    opt[Unit]("singleThreadedDebugMode")
      .action((_, c) => c.copy(isSingleThreadedDebugMode = true))
      .text(
        """Run DBSubsetter in debug mode (NOT recommended)
          |                           Uses a simplified architecture which avoids akka-streams and parallel computations
          |                           Ignores `--originDbParallelism` and `--targetDbParallelism` and uses one connection per database
          |                           Subsetting may be significantly slower
          |                           The resulting subset should be exactly the same as in regular mode
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
        |        --originDbParallelism 10 \
        |        --targetDbParallelism 10
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
        |        --originDbParallelism 15 \
        |        --targetDbParallelism 20
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
        |        --originDbParallelism 1 \
        |        --targetDbParallelism 1
        |
        |
        |   # Allowing DBSubsetter to use up to 8 gigabytes of memory:
        |      java -Xmx8G -jar /path/to/DBSubsetter.jar [...]
        |
        |Notes:
        |
        |   # Arguments containing whitespace must be enclosed in quotes:
        |      (OK)    --schemas public,audit,finance
        |      (OK)    --schemas "public, audit, finance"
        |      (OK)    --schemas 'public, audit, finance'
        |      (OK)    --schemas "public","audit","finance"
        |      (ERROR) --schemas public, audit, finance
        |      (ERROR) --schemas "public", "audit", "finance"
        |
        |   # Arguments containing a quotation mark must either alternate single and double quotes or use backslash escaping:
        |      (OK)    --baseQuery 'primary_schools."Districts" ::: "Districts"."Id" in (2, 78, 945) ::: includeChildren'
        |      (OK)    --baseQuery "primary_schools.\"Districts\" ::: \"Districts\".\"Id\" in (2, 78, 945) ::: includeChildren"
        |      (ERROR) --baseQuery "primary_schools."Districts" ::: "Districts"."Id" in (2, 78, 945) ::: includeChildren"
        |""".stripMargin
    note(usageExamples)
  }
}

case class CmdLineForeignKey(fromSchema: SchemaName,
                             fromTable: TableName,
                             fromColumns: List[ColumnName],
                             toSchema: SchemaName,
                             toTable: TableName,
                             toColumns: List[ColumnName])

case class CmdLinePrimaryKey(schema: SchemaName,
                             table: TableName,
                             columns: List[ColumnName])
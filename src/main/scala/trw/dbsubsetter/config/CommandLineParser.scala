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
      .action((cs, c) => c.copy(originDbConnectionString = cs))
      .text(
        """JDBC connection string to the full-size origin db, for example:
          |                           MySQL:       jdbc:mysql://<originHost>:<originPort>/<originDb>?user=<originUser>&password=<originPassword>&rewriteBatchedStatements=true
          |                           PostgreSQL:  jdbc:postgresql://<originHost>:<originPort>/<originDb>?user=<originUser>&password=<originPassword>
          |                           SQL Server:  jdbc:sqlserver://<originHost>:<originPort>;databaseName=<originDb>;user=<originUser>;password=<originPassword>
          |""".stripMargin)

    opt[String]("targetDbConnStr")
      .valueName("jdbc connection string>")
      .required()
      .action((cs, c) => c.copy(targetDbConnectionString = cs))
      .text(
        """JDBC connection string to the smaller target db db, for example:
          |                           MySQL:       jdbc:mysql://<targetHost>:<targetPort>/<targetDb>?user=<targetUser>&password=<targetPassword>&rewriteBatchedStatements=true
          |                           PostgreSQL:  jdbc:postgresql://<targetHost>:<targetPort>/<targetDb>?user=<targetUser>&password=<targetPassword>
          |                           SQL Server:  jdbc:sqlserver://<targetHost>:<targetPort>;databaseName=<targetDb>;user=<targetUser>;password=<targetPassword>
          |""".stripMargin)

    opt[String]("baseQuery")
      .required()
      .maxOccurs(Int.MaxValue)
      .valueName("<schema>.<table> ::: <whereClause> ::: <fetchChildren (true/false)>")
      .action { case (bq, c) =>
        val r = """^\s*(.+)\.(.+)\s+:::\s+(.+)\s+:::\s+(true|false)\s*$""".r
        bq match {
          case r(schema, table, whereClause, fetchChildren) =>
            val fc = fetchChildren == "true"
            c.copy(baseQueries = ((schema.trim, table.trim), whereClause.trim, fc) :: c.baseQueries)
          case _ => throw new RuntimeException()
        }
      }
      .text(
        """Starting table, where-clause, and fetchChildren to kick off subsetting
          |                           About fetchChildren:
          |                              It works recursively. So the children of the children will also be fetched, and so on
          |                              `true` is recommended for most common use cases
          |                              `false` works in edge cases such as including a whole table: --baseQuery public.invoice_types ::: true ::: false
          |                              That example includes the entire invoice_types table but would *not* fetch any of its children
          |                           Can be specified multiple times
          |""".stripMargin)

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
        """Foreign keys to enforce during subsetting even though they are not defined in the database
          |                           Optionally specify a "where clause" to additionally restrict the defined foreign key as needed
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
        """Primary key to recognize during subsetting when it is not defined in the database
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
        """Exclude a list of columns from the resulting subsetted data
          |                           Intended only for columns that are not part of any primary keys or foreign keys
          |                           Useful as a workaround if DBSubsetter does not support a vendor-specific data type
          |                           Can be specified multiple times
          |""".stripMargin)

    opt[Int]("originDbParallelism")
      .valueName("<int>")
      .required()
      .action((dbp, c) => c.copy(originDbParallelism = dbp))
      .text("Maximum number of simultaneous open connections to the full-size origin DB\n")

    opt[Int]("targetDbParallelism")
      .valueName("<int>")
      .required()
      .action((dbp, c) => c.copy(targetDbParallelism = dbp))
      .text("Maximum number of simultaneous open connections to the smaller target DB\n")

    opt[Unit]("singleThreadedDebugMode")
      .action((_, c) => c.copy(isSingleThreadedDebugMode = true))
      .text(
        """Run DBSubsetter in debug mode (NOT recommended)
          |                               Uses a simple single-threaded setup, avoids akka-streams and parallel computations
          |                               Ignores `originDbParallelism` and `targetDbParallelism` settings and uses just 1 connection to each
          |                               Subsetting may be significantly slower
          |                               The resulting subset should be exactly the same as in regular mode
          |""".stripMargin)

    private val usageExamples =
      """
        |Examples:
        |
        |   # With simple configuration:
        |   java -jar /path/to/DBSubsetter.jar \
        |     --schemas "public" \
        |     --originDbConnStr "jdbc:postgresql://localhost:5450/origin_db_name?user=yourUser&password=yourPassword" \
        |     --targetDbConnStr "jdbc:postgresql://localhost:5451/target_db_name?user=yourUser&password=yourPassword" \
        |     --baseQuery "public.users ::: id % 100 = 0 ::: true" \
        |     --originDbParallelism 10 \
        |     --targetDbParallelism 10
        |
        |
        |   # With multiple starting conditions (a.k.a. multiple "base queries"):
        |   java -jar /path/to/DBSubsetter.jar \
        |     --schemas "public","audit","finance" \
        |     --originDbConnStr "jdbc:postgresql://localhost:5450/origin_db_name?user=yourUser&password=yourPassword" \
        |     --targetDbConnStr "jdbc:postgresql://localhost:5451/target_db_name?user=yourUser&password=yourPassword" \
        |     --baseQuery "public.students ::: student_id in (select v.student_id from valedictorians as v where v.year = 2017) ::: true", \
        |     --baseQuery "public.users ::: random() < 0.001 :::", \
        |     --baseQuery "finance.transactions ::: created_at < '2017-12-25' ::: false" \
        |     --originDbParallelism 15 \
        |     --targetDbParallelism 20
      """.stripMargin
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
package trw.dbsubsetter.config

import scopt.OptionParser
import trw.dbsubsetter.db.{ColumnName, SchemaName, TableName}

object CommandLineParser {
  val parser: OptionParser[Config] = new OptionParser[Config]("DBSubsetter") {
    head("DBSubsetter", "0.1")
    help("help").text("Prints this usage text\n")
    version("version").text("Prints the application version\n")

    opt[Seq[String]]("schemas")
      .valueName("<schema1,schema2,schema3,etc.>")
      .action((s, c) => c.copy(schemas = s))
      .required()
      .text("Names of the schemas to include when subsetting\n")

    opt[String]("originDbConnStr")
      .required()
      .action((cs, c) => c.copy(originDbConnectionString = cs))
      .text("JDBC connection string to the full-size origin db\n")

    opt[String]("targetDbConnStr")
      .required()
      .action((cs, c) => c.copy(targetDbConnectionString = cs))
      .text("JDBC connection string to the smaller target db db\n")

    opt[String]("baseQuery")
      .required()
      .maxOccurs(Int.MaxValue)
      .valueName("<schema.table>=<whereClause>")
      .action { case (bq, c) =>
        val r = """(.+)\.([^=]+)\=(.+)""".r
        bq match {
          case r(schema, table, whereClause) =>
            c.copy(baseQueries = ((schema, table), whereClause) :: c.baseQueries)
          case _ => throw new RuntimeException()
        }
      }
      .text(
        """Starting table and where-clause to kick off subsetting
          |                           Can be specified multiple times
          |                           """.stripMargin)

    opt[String]("foreignKey")
      .maxOccurs(Int.MaxValue)
      .valueName("<schema1.table1(column1, column2, column3)> ::: schema2.table2(column4, column5, column6)")
      .action { case (fk, c) =>
        val r = """(.+)\.(.+)\((.+)\)\s*:::\s*(.+)\.(.+)\((.+)\)""".r
        fk match {
          case r(fromSchema, fromTable, fromColumns, toSchema, toTable, toColumns) =>
            val fk = CommandLineStandardForeignKey(
              fromSchema,
              fromTable,
              fromColumns.split(",").toList,
              toSchema,
              toTable,
              toColumns.split(",").toList
            )
            c.copy(cmdLineStandardFks = fk :: c.cmdLineStandardFks)
          case _ => throw new RuntimeException()
        }
      }
      .text(
        """Foreign keys to enforce during subsetting even though they are not defined in the database
          |                           Can be specified multiple times
          |                           """.stripMargin)

    opt[String]("whereClauseForeignKey")
      .maxOccurs(Int.MaxValue)
      .valueName("<schema1.table1 ::: schema2.table2 ::: whereClause>")
      .action { case (fk, c) =>
        val r = """(.+)\.(.+)\s*:::\s*(.+)\.(.+)\s*:::\s*(.+)""".r
        fk match {
          case r(fromSchema, fromTable, toSchema, toTable, whereClause) =>
            val fk = CommandLineWhereClauseForeignKey(
              fromSchema,
              fromTable,
              toSchema,
              toTable,
              whereClause
            )
            c.copy(cmdLineWhereClauseFks = fk :: c.cmdLineWhereClauseFks)
          case _ => throw new RuntimeException()
        }
      }
      .text(
        """Foreign keys to enforce during subsetting even though they are not defined in the database
          |                           For use cases that are too complex to be expressed with the more limited `--foreignKey` option
          |                           Can be specified multiple times
        """.stripMargin)

    opt[Int]("originDbParallelism")
      .required()
      .action((dbp, c) => c.copy(originDbParallelism = dbp))
      .text("Maximum number of simultaneous open connections to the full-size origin DB\n")

    opt[Int]("targetDbParallelism")
      .required()
      .action((dbp, c) => c.copy(targetDbParallelism = dbp))
      .text("Maximum number of simultaneous open connections to the smaller target DB\n")

    private val usageExamples =
      """
        |Examples:
        |
        |   # With simple configuration:
        |   java -jar /path/to/DBSubsetter.jar \
        |     --schemas "public" \
        |     --originDbConnStr "jdbc:postgresql://localhost:5450/origin_db_name?user=yourUser&password=yourPassword" \
        |     --targetDbConnStr "jdbc:postgresql://localhost:5451/target_db_name?user=yourUser&password=yourPassword" \
        |     --baseQuery "public.users=id % 100 = 0"
        |
        |
        |   # With multiple starting conditions (a.k.a. multiple "base queries"):
        |   java -jar /path/to/DBSubsetter.jar \
        |     --schemas "public","audit","finance" \
        |     --originDbConnStr "jdbc:postgresql://localhost:5450/origin_db_name?user=yourUser&password=yourPassword" \
        |     --targetDbConnStr "jdbc:postgresql://localhost:5451/target_db_name?user=yourUser&password=yourPassword" \
        |     --baseQuery "public.students=student_id in (select v.student_id from valedictorians as v where v.year = 2017)", \
        |     --baseQuery "public.users=random() < 0.001", \
        |     --baseQuery "finance.transactions=finance.transactions.created_at < '2017-12-25'"
      """.stripMargin
    note(usageExamples)
  }
}

case class CommandLineStandardForeignKey(fromSchema: SchemaName,
                                         fromTable: TableName,
                                         fromColumns: List[ColumnName],
                                         toSchema: SchemaName,
                                         toTable: TableName,
                                         toColumns: List[ColumnName])

case class CommandLineWhereClauseForeignKey(fromSchema: SchemaName,
                                            fromTable: TableName,
                                            toSchema: SchemaName,
                                            toTable: TableName,
                                            whereClause: String)
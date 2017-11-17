package trw.dbsubsetter.config

import scopt.OptionParser

object CommandLineParser {
  val parser: OptionParser[Config] = new OptionParser("DBSubsetter") {
    head("DBSubsetter", "0.1")
    help("help").text("prints this usage text")
    version("version").text("prints the application version")

    opt[Seq[String]]("schemas")
      .valueName("<schema1,schema2,schema3,etc.>")
      .action((s, c) => c.copy(schemas = s))
      .required()
      .text("names of the schemas to include when subsetting")

    opt[String]("originDbConnStr")
      .required()
      .action((cs, c) => c.copy(originDbConnectionString = cs))
      .text("JDBC connection string to the original full-size db")

    opt[String]("targetDbConnStr")
      .required()
      .action((cs, c) => c.copy(targetDbConnectionString = cs))
      .text("JDBC connection string to the resulting small db")

    opt[String]("baseQuery")
      .required()
      .maxOccurs(Int.MaxValue)
      .valueName("<schema.table>=<whereClause>")
      .action { case (bq, c) =>
        val Array(fullyQualifiedTable, whereClause) = bq.split("=", 2)
        val Array(schema, table) = fullyQualifiedTable.split("""\.""", 2)
        c.copy(baseQueries = ((schema, table), whereClause) :: c.baseQueries)
      }
      .text("Starting table and where-clause to kick off subsetting. Can be specified multiple times.")

    opt[Int]("originDbParallelism")
      .required()
      .action((dbp, c) => c.copy(originDbParallelism = dbp))
      .text("Maximum number of simultaneous open connections to origin DB")

    opt[Int]("targetDbParallelism")
      .required()
      .action((dbp, c) => c.copy(targetDbParallelism = dbp))
      .text("Maximum number of simultaneous open connections to target DB")

    private val usageExamples =
      """
        |Examples:
        |
        |   java -jar /path/to/DBSubsetter.jar \
        |     --schemas "public" \
        |     --originDbConnStr "jdbc:postgresql://localhost:5450/origin_db_name?user=yourUser&password=yourPassword" \
        |     --targetDbConnStr "jdbc:postgresql://localhost:5451/target_db_name?user=yourUser&password=yourPassword" \
        |     --baseQueries "public.users"="public.users.id % 100 = 0"
        |
        |   or
        |
        |   java -jar /path/to/DBSubsetter.jar \
        |     --schemas "public","audit","finance" \
        |     --originDbConnStr "jdbc:postgresql://localhost:5450/origin_db_name?user=yourUser&password=yourPassword" \
        |     --targetDbConnStr "jdbc:postgresql://localhost:5451/target_db_name?user=yourUser&password=yourPassword" \
        |     --baseQueries \
        |       "public.students"="public.students.student_id in (select v.student_id from valedictorians as v where v.year = 2017)", \
        |       "public.users"="random() < 0.001", \
        |       "finance.transactions"="finance.transactions.created_at < '2017-12-25'"
      """.stripMargin
    note(usageExamples)
  }
}

package trw.dbsubsetter

object CommandLineParser {
  val parser: scopt.OptionParser[Config] = new scopt.OptionParser[Config]("DBSubsetter") {
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

    opt[Map[FullyQualifiedTableName, WhereClause]]("baseQueries")
      .required()
      .valueName("<schema1.table1>=<whereClause1>,<schema2.table2>=<whereClause2>")
      .validate(m => if (m.keys.forall(str => str.split("\\.").length == 2)) success else failure("Invalid format for --baseQueries"))
      .action((bq, c) => c.copy(
        baseQueries = bq.map { case (fqtn, whereClause) =>
          val Array(schemaName, tableName) = fqtn.split("\\.")
          (schemaName, tableName) -> whereClause
        }
      ))
      .text("Starting tables and where-clauses for initial queries to kick off subsetting.")

    opt[Int]("dbParallelism")
      .action((dbp, c) => c.copy(dbParallelism = dbp))
      .text("Maximum number of simultaneous open connections to origin DB")

    val usageExamples =
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

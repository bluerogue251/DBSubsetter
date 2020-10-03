# DBSubsetter

DBSubsetter is a tool for taking a logically consistent subset of a relational database.

Starting with a given set of rows, it respects foreign key constraints by recursively fetching
the parents and (optionally) the children of those rows.
This is useful for creating local development and testing datasets, or for exporting the
data belonging only to a particular group of users.


## Project Goals

_Easy to learn_: a simple and well documented command line interface.

_Support large datasets_: designed for stability when handling large datasets.

_Non-intrusive_: connections to the origin DB are read-only. Writes to the
                 target DB require only [DML](https://stackoverflow.com/a/2578207)
                 permission.

_Deterministic_: identical inputs yield identical outputs.

_Do one thing well_: a tiny codebase focused exclusively on core subsetting features.


## Supported Databases

DBSubsetter has been tested against recent versions of PostgreSQL, MySQL, and Microsoft SQL Server.

Please [open an issue](https://github.com/bluerogue251/DBSubsetter/issues/new)
if you would like support for a different database vendor.


## Download and Usage Instructions

1. Load an empty schema from your "origin" database into your "target" database.
   Follow vendor-specific instructions for:
     * [Postgres](docs/pre_subset_postgres.md)
     * [MySQL](docs/pre_subset_mysql.md)
     * [Microsoft SQL Server](docs/pre_subset_ms_sql_server.md)
 
2. Use Java 8 or above to run our
[latest release](https://github.com/bluerogue251/DBSubsetter/releases/latest):

```bash
# Download the DBSubsetter.jar file
$ wget https://github.com/bluerogue251/DBSubsetter/releases/download/v1.0.0-beta.4/DBSubsetter.jar --output-document /path/to/DBSubsetter.jar
 
# Show explanation and examples of how to configure multiple schemas, 
# multiple base queries, missing foreign or primary keys, columns to exclude,
# vendor-specific JDBC connection strings, etc.
$ java -jar /path/to/DBSubsetter.jar --help

# Once you are comfortable with the syntax and options, run DBSubsetter for real
$ java -jar /path/to/DBSubsetter.jar \
    --schemas schema_1,schema_2 \
    --originDbConnStr "jdbc:<driverName>://<originConnectionString>" \
    --targetDbConnStr "jdbc:<driverName>://<targetConnectionString>" \
    --baseQuery "your_schema.users ::: id % 100 = 0 ::: includeChildren" \
    --keyCalculationDbConnectionCount 8 \
    --dataCopyDbConnectionCount 8
```

3. After DBSubsetter exits, follow vendor-specific instructions for:
    * [Postgres](docs/post_subset_postgres.md)
    * [MySQL](docs/post_subset_mysql.md)
    * [Microsoft SQL Server](docs/post_subset_ms_sql_server.md)


## Contributing

Contributions of all kinds are welcome!
To ask a question, report a bug, or request a feature, please
[open an issue](https://github.com/bluerogue251/DBSubsetter/issues/new).
To contribute code changes, please
[open a pull request](https://github.com/bluerogue251/DBSubsetter/pulls).

Please follow our [code of conduct](CODE_OF_CONDUCT.md) when contributing.


## Related Projects

DBSubsetter was inspired by
[Jailer](http://jailer.sourceforge.net/home.htm) and
[rdbms-subsetter](https://github.com/18F/rdbms-subsetter).
Other related resources include
[db_subsetter](https://github.com/lostapathy/db_subsetter), 
[DataBee](https://www.databee.com/),
[pg_sample](https://github.com/mla/pg_sample),
[DATPROF](http://www.datprof.com/products/datprof-subset/),
[abridger](https://github.com/freewilll/abridger),
[postgres-subset](https://github.com/BeautifulDestinations/postgres-subset), and
[CA Data Subset](https://docops.ca.com/ca-test-data-manager/4-2/en/provisioning-test-data/subset-production-data).

DBSubsetter is written in
[Scala](https://www.scala-lang.org/) using
[Akka Streams](https://doc.akka.io/docs/akka/2.5.8/stream/index.html?language=scala),
[Chronicle-Queue](https://github.com/OpenHFT/Chronicle-Queue), and
[scopt](https://github.com/scopt/scopt).
[Slick](http://slick.lightbend.com/) is used for testing.

## License

DBSubsetter is released under the [MIT License](LICENSE.txt).
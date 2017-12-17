# DBSubsetter

DBSubsetter is a tool for taking a logically consistent subset of a relational database.

Starting with a base condition specifying a set of rows from one or more tables, it respects foreign key constraints by recursively fetching the "parents" and (optionally) the "children" of those rows.

This is useful for local development and testing or for exporting all the data belonging to a particular set of users or customers for debugging and sharing.


## Project Goals

_High performance_: Optimized for fast runtimes on large datasets.

_Deterministic_: Identical inputs yield identical outputs. Random subsets are possible, but only if purposely configured.

_Do one thing well_: A tiny codebase with a highly focused and well-tested set of features.


## Supported Databases

DBSubsetter has been tested against recent versions of PostgreSQL, MySQL, and Microsoft SQL Server.

Feel free to open a GitHub ticket if you would like support for a different database vendor.


## Download / Installation / Usage

1. Load an empty schema from your "origin" database into your "target" database. See vendor-specific instructions for [Postgres](docs/pre_subset_postgres.md), [MySQL](docs/pre_subset_mysql.md), and [Microsoft SQL Server](docs/pre_subset_ms_sql_server.md).
 
2. Download the DBSubsetter.jar file from our [latest release](https://github.com/bluerogue251/DBSubsetter/releases/latest) and run it with Java 8:

```bash
# Download the DBSubsetter.jar file
$ wget https://github.com/bluerogue251/DBSubsetter/releases/download/v1.0.0-beta.2/DBSubsetter.jar --output-document /path/to/DBSubsetter.jar
 
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
    --originDbParallelism 8 \
    --targetDbParallelism 8
```

3. After DBSubsetter exits, do any last steps as necessary. See vendor-specific instructions for [Postgres](docs/post_subset_postgres.md), [MySQL](docs/post_subset_mysql.md), and [Microsoft SQL Server](docs/post_subset_ms_sql_server.md).


## Resource consumption

Memory usage in the worst case will be proportional to the sum of:

* The size of all primary keys in the target database. (But depending on your 
  schema structure, you may be able to use the `--skipPkStore` option to
  configure DBSubsetter to use just a small fraction of that amount.)
* The size of your largest single query result multiplied by 
  (`--originDbParallelism` + `--targetDbParallelism` + `--preTargetBufferSize`)

Disk usage (in tempfiles) will be proportional to the size of all foreign keys in the target database.

## Contributing

Contributions of all kinds are welcome!

Whether it is to fix a typo, improve the documentation, report or fix a bug, add a new feature, or whatever else you have in mind, feel free to open an issue or a pull request on the project [GitHub page](https://github.com/bluerogue251/DBSubsetter).

The only condition for contributing to this project is to follow our [code of conduct](CODE_OF_CONDUCT.md) so that everyone is treated with respect.


## Related projects and acknowledgments

DBSubsetter was inspired by and borrowed ideas from
[Jailer](http://jailer.sourceforge.net/home.htm) and
[rdbms-subsetter](https://github.com/18F/rdbms-subsetter).

Here are some other similar or related resources:
[db_subsetter](https://github.com/lostapathy/db_subsetter), 
[DataBee](https://www.databee.com/),
[pg_sample](https://github.com/mla/pg_sample),
[DATPROF](http://www.datprof.com/products/datprof-subset/),
[abridger](https://github.com/freewilll/abridger), and
[postgres-subset](https://github.com/BeautifulDestinations/postgres-subset).

DBSubsetter is written in [Scala](https://www.scala-lang.org/) using
[Akka Streams](https://doc.akka.io/docs/akka/2.5.8/stream/index.html?language=scala),
[Chronicle-Queue](https://github.com/OpenHFT/Chronicle-Queue),
[scopt](https://github.com/scopt/scopt), and
[Slick](http://slick.lightbend.com/).

## License

DBSubsetter is released under the [MIT License](LICENSE.txt).
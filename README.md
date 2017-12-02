# DBSubsetter

DBSubsetter is a tool for taking a logically consistent subset of a relational database.

Starting with a base condition specifying a set of rows from one or more tables, it respects foreign key constraints by recursively fetching the "parents" and (optionally) the "children" of those rows.

This is useful for local development and testing or for exporting all the data belonging to a particular set of users or customers for debugging and sharing.


## Project Goals

* High performance: optimized to run as fast as possible and to take full advantage of multi-core machines.
* Determinism: identical inputs should yield identical outputs. Random subsets are possible, but only if purposely configured.
* Do one thing well: a tiny codebase with a small and focused set of features.


## Supported Databases

DBSubsetter has been tested against recent versions of PostgreSQL, MySQL, and Microsoft SQL Server.

Feel free to open a GitHub ticket if you would like support for a different database vendor.


## Download / Installation / Usage

1. Load an empty schema from your "origin" database into your "target". See vendor-specific instructions for [Postgres](docs/pre_subset_postgres.md), [MySQL](docs/pre_subset_mysql.md), and [Microsoft SQL Server](docs/pre_subset_ms_sql_server.md).
 
2. Download and run the JAR of the [latest release of DBSubsetter](https://github.com/bluerogue251/DBSubsetter/releases/latest):

```sh
# Download the DBSubsetter JAR
$ curl https://github.com/bluerogue251/DBSubsetter/releases/latest > /path/to/DBSubsetter.jar
 
# Show explanations and examples of all available options, including how to configure:
# multiple schemas, multiple base queries, missing foreign or primary keys, 
# columns to ignore, proper syntax for vendor-specific JDBC connection strings, etc.
$ java -jar /path/to/DBSubsetter.jar --help

# Once you are comfortable with the syntax and options, run DBSubsetter for real
$ java -jar /path/to/DBSubsetter.jar \
    --schemas "your_schema" \
    --originDbConnStr "jdbc:<driverName>://<originConnectionString>" \
    --targetDbConnStr "jdbc:<driverName>://<targetConnectionString>" \
    --baseQuery "your_schema.users ::: id % 100 = 0 ::: true" \
    --originDbParallelism 8 \
    --targetDbParallelism 8
```

3. After DBSubsetter exits, do some last steps and cleanup. See vendor-specific instructions for [Postgres](docs/post_subset_postgres.md), [MySQL](docs/post_subset_mysql.md), and [Microsoft SQL Sever](docs/post_subset_ms_sql_server.md).


## Resource consumption

Memory usage will be proportional to the size of all the primary keys in the target database. Temporary spikes above this amount are also possible.


## Contributing

Contributions of all kinds are welcome and appreciated here!

Whether it is to fix a typo, improve the documentation, add more tests, report or fix a bug, add a new feature, or whatever else you have in mind, feel free to open an issue or a pull request on GitHub.

The only condition for contributing to this project is to follow our [code of conduct](CODE_OF_CONDUCT.md) so that everyone is treated with respect.


## Related projects and resources

DBSubsetter was inspired by and borrowed ideas from:

* [Jailer](http://jailer.sourceforge.net/home.htm)
* [rdbms-subsetter](https://github.com/18F/rdbms-subsetter)

Here are some other similar or related resources:

* [DataBee](https://www.databee.com/)
* [pg_sample](https://github.com/mla/pg_sample)
* [This stack overflow question](https://stackoverflow.com/questions/3980379/how-to-export-consistent-subset-of-database)
* [DATPROF](http://www.datprof.com/products/datprof-subset/)
* [db_subsetter](https://github.com/lostapathy/db_subsetter)
* [abridger](https://github.com/freewilll/abridger)
* [postgres-subset](https://github.com/BeautifulDestinations/postgres-subset)


## License

DBSubsetter is released under the [MIT License](LICENSE.txt).
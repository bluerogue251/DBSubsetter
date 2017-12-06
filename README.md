# DBSubsetter

DBSubsetter is a tool for taking a logically consistent subset of a relational database.

Starting with a base condition specifying a set of rows from one or more tables, it respects foreign key constraints by recursively fetching the "parents" and (optionally) the "children" of those rows.

This is useful for local development and testing or for exporting all the data belonging to a particular set of users or customers for debugging and sharing.


## Project Goals

_High performance_: Optimized to take advantage of parallelism for fast runtimes on large datasets.

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
$ wget https://github.com/bluerogue251/DBSubsetter/releases/download/v1.0.0-beta.1/DBSubsetter.jar --output-document /path/to/DBSubsetter.jar
 
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

Memory usage will be proportional to the size of all the primary keys in the target database. Temporary spikes above this amount are also possible.


## Contributing

Contributions of all kinds are welcome!

Whether it is to fix a typo, improve the documentation, report or fix a bug, add a new feature, or whatever else you have in mind, feel free to open an issue or a pull request on the project [GitHub page](https://github.com/bluerogue251/DBSubsetter).

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
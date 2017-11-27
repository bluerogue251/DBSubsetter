# DBSubsetter

DBSubsetter is a tool for taking a logically consistent subset of the contents of a relational database.

Starting a set of rows from a particular table, it recursively fetches the "parents" and optionally also the "children" of those rows, respecting foreign key constraints.

This is useful for making a small copy of a production database to distribute to developers for local development and testing use, or for fetching all the data related to a particular set of users or customers for debugging or sharing.

As guiding principles, this project aims to:

1. Be deterministic -- identical inputs yield identical outputs. Random subsets are possible, but only if purposely configured.
2. Be highly performant -- the more cores you have, the faster it should be.
3. Do one thing well: a tiny codebase with a very small set of features.

# Supported Databases

DBSubsetter has only been tested against recent versions of PostgreSQL. Support for MySQL, Oracle, and SQL Server is coming soon.

# Download / installation / usage
  
Load just the empty, "pre-data" table structure -- no foreign key constraints nor indices yet -- from your "origin" database into your "target":

`pg_dump --host origin_host --port 5432 --user origin_user --dbname origin_db_name --section pre-data --file pre-data-dump.sql`

`psql --host target_host --port 5432 --user target_user --dbname target_db_name --file pre-data-dump.sql`

Download and run the [latest release](https://github.com/not-there-yet) on the command line:

```
$ java -jar /path/to/DBSubsetter.jar \
    --schemas "public" \
    --originDbConnStr "jdbc:postgresql://<originHost>:<originPort>/<originDbName>?user=<originDbUser>&password=<originDbPassword>" \
    --targetDbConnStr "jdbc:postgresql://<targetHost>:<targetPort>/<targetDbName>?user=<targetDbUser>&password=<targetDbPassword>" \
    --baseQuery "public.users ::: id % 100 = 0 ::: true" \
    --originDbParallelism 10 \
    --targetDbParallelism 10
```

For a detailed explanation of all available options and examples of complex configurations with multiple schemas, multiple base queries, specifying missing foreign or primary keys, ignoring certain columns, etc., run:

```
$ java -jar /path/to/DBSubsetter.jar --help
```

Once DBSubetter has completed, add in foreign key constraints, indices, etc to your target database:

`pg_dump --host origin_host --port 5432 --user origin_user --dbname origin_db_name --section post-data --format custom --file post-data-dump.pgdump`

`pg_restore --host target_host --port 5432 --user target_user --dbname target_db_name --jobs 10 post-data-dump.pgdump`

Remember to also reset primary key sequence values.

# Resource consumption

Memory usage will be proportional to the size of a collection of all the primary keys contained in the target database. Temporary spikes above this amount are also possible.

# Contributing

Contributions of all kinds are welcome and appreciated here!

Whether it is to fix a typo, improve the documentation, add more tests, fix a bug, add a new feature, or whatever else you have in mind, feel free to open a pull request on GitHub.

The only condition for contributing to this project is to follow our [code of conduct](CODE_OF_CONDUCT.md) so that everyone is treated with respect.

# Related projects and resources

* [Jailer](http://jailer.sourceforge.net/home.htm)
* [rdbms-subsetter](https://github.com/18F/rdbms-subsetter)
* [DataBee](https://www.databee.com/)
* [pg_sample](https://github.com/mla/pg_sample)
* [This stack overflow question](https://stackoverflow.com/questions/3980379/how-to-export-consistent-subset-of-database)
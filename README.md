# DBSubsetter

DBSubsetter is a tool for taking a logically consistent subset of the contents of a relational database.

Starting with a set of rows from a particular table, it respects foreign key constraints by recursively fetching the "parents" and (optionally) the "children" of those rows.

It is useful for making a small copy of a production database for local development and testing, or for exporting all the data belonging to a particular set of users or customers for debugging or sharing.


## Design principles

* Be deterministic: identical inputs should yield identical outputs. Random subsets are possible, but only if purposely configured.
* Be highly performant: the more cores you have, the faster it should be.
* Do one thing well: a tiny codebase with a small and focused set of features.

## Supported Databases

DBSubsetter has only been tested against recent versions of PostgreSQL. Support for MySQL, Oracle, and SQL Server is coming soon.

## Download / installation / usage
  
Load the empty table structure from your "origin" database into your "target". No foreign keys or indices should be in the "target" db yet:

```sh
$ pg_dump --host origin_host --port 5432 --user origin_user --dbname origin_db_name --section pre-data --file pre-data-dump.sql

$ psql --host target_host --port 5432 --user target_user --dbname target_db_name --file pre-data-dump.sql
```


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

For explanations of all available options and examples of complex configurations with multiple schemas, multiple base queries, specifying missing foreign or primary keys, ignoring certain columns, etc., run:

```
$ java -jar /path/to/DBSubsetter.jar --help
```

Once DBSubetter has completed, add in foreign key constraints, indices, etc to your target database:

```sh
$ pg_dump --host origin_host --port 5432 --user origin_user --dbname origin_db_name --section post-data --format custom --file post-data-dump.pgdump

$ pg_restore --host target_host --port 5432 --user target_user --dbname target_db_name --jobs 10 post-data-dump.pgdump

# Remember to also reset primary key sequence values.
```


## Resource consumption

Memory usage will be proportional to the size of all the primary keys in the target database. Temporary spikes above this amount are also possible.

## Contributing

Contributions of all kinds are welcome and appreciated here!

Whether it is to fix a typo, improve the documentation, add more tests, fix a bug, add a new feature, or whatever else you have in mind, feel free to open a pull request on GitHub.

The only condition for contributing to this project is to follow our [code of conduct](CODE_OF_CONDUCT.md) so that everyone is treated with respect.

## Related projects and resources

* [Jailer](http://jailer.sourceforge.net/home.htm)
* [rdbms-subsetter](https://github.com/18F/rdbms-subsetter)
* [DataBee](https://www.databee.com/)
* [pg_sample](https://github.com/mla/pg_sample)
* [This stack overflow question](https://stackoverflow.com/questions/3980379/how-to-export-consistent-subset-of-database)
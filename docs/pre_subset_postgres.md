## Pre-Subset Instructions: PostgreSQL

Load the empty table structure from your "origin" database into your "target".

Foreign keys and indices should be excluded from this step. They will be added to the "target" db in a later step, after subsetting has finished. This makes inserts into your target database significantly faster for large datasets. Temporarily avoiding foreign keys also greatly simplifies DBSubsetter's logic for inserting data

```sh
# Dump out an empty schema from your "origin" database into a file called `pre-data-dump.sql`
$ pg_dumpall --host origin_host --port 5432 --user origin_user --dbname origin_db_name --section pre-data --file pre-data-dump.sql

# Load the contents of the `pre-data-dump.sql` file into your "target" database
$ psql --host target_host --port 5432 --user target_user --dbname target_db_name --file pre-data-dump.sql
```

For some users, especially those on AWS RDS or without superuser access, `pg_dumpall` may error out on Postgres versions < 10. See [here](http://www.thatguyfromdelhi.com/2016/12/custom-pgdumpall-now-works-with-aws.html) for details.
Substituting `pg_dump` instead of `pg_dumpall` is one possible workaround for this. However, `pg_dump` ignores roles, permissions, and other "global objects" in Postgres,
so any porting over of roles and permissions to the target database must be done manually.
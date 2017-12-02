## PostgreSQL Pre-Subset Instructions

Load the empty table structure from your "origin" database into your "target".

Foreign keys and indices should be excluded from this step. There two reasons for this:

1) It makes inserts into your target database significantly faster for large datasets
2) Temporarily avoiding foreign keys greatly simplifies DBSubsetter's logic for inserting data

The foreign keys and indices will be added to the "target" db in a later step, after subsetting has finished.

```sh
$ pg_dumpall --host origin_host --port 5432 --user origin_user --dbname origin_db_name --section pre-data --file pre-data-dump.sql

$ psql --host target_host --port 5432 --user target_user --dbname target_db_name --file pre-data-dump.sql
```

For some users, especially those on AWS RDS or without superuser access, `pg_dumpall` may error out on Postgres versions < 10. See [here](http://www.thatguyfromdelhi.com/2016/12/custom-pgdumpall-now-works-with-aws.html) for details.
Substituting `pg_dump` instead of `pg_dumpall` is one possible workaround for this. However, `pg_dump` ignores roles, permissions, and other "global objects" in Postgres,
so any porting over of roles and permissions to the target database must be done manually.
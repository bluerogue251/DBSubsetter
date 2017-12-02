## Pre-Subset Instructions: PostgreSQL

Load the empty table structure from your "origin" database into your "target":

```sh
# Dump out all postgres roles into a file called `roles.sql`
$ pg_dumpall --roles-only --host=<originHost> --port=<originPort> --username=<originUser> --database=<originDb> --file roles.sql

# Load `roles.sql` into your "target" database
$ psql --host <targetHost> --port <targetPort> --user <targetUser> --dbname <targetDb> --file roles.sql

# Dump out just the schema (no data) from your "origin" database into a file called `pre-data-dump.sql`
$ pg_dump --host <originHost> --port <originPort> --user <originUser> --dbname <originDbName> --section pre-data --file pre-data-dump.sql

# Load `pre-data-dump.sql` into your "target" database
$ psql --host <targetHost> --port <targetPort> --user <targetUser> --dbname <targetDb> --file pre-data-dump.sql
```

### Notes

Foreign keys and indices are purposely excluded from this step. They will be added to the "target" db in a later step, after subsetting has finished. This makes inserts into your target database significantly faster for large datasets. Temporarily avoiding foreign keys also greatly simplifies DBSubsetter's logic for inserting data.

For users without superuser access to the "origin" database (for example, those using Amazon RDS), `pg_dumpall` may error out on Postgres versions < 10 when trying to dump out the roles. See [here](http://www.thatguyfromdelhi.com/2016/12/custom-pgdumpall-now-works-with-aws.html) for details.
In this case, either the porting over of roles to the "target" database must be done manually, or `pg_dump` can be run with the `--no-privileges` and `--no-owner` options when dumping out `pre-data-dump.sql` so that missing roles are ignored. Be careful, because in the latter case, your "target" database
may end up with different user privilege logic than your "origin" database.

The above commands have been tested against `pg_dump` and `pg_dumpall` version 9.6.6. Commands might be slightly different if you are using a different version of these tools. See the [pg_dump docs](https://www.postgresql.org/docs/current/static/app-pg-dumpall.html) and the [pg_dumpall_docs](https://www.postgresql.org/docs/current/static/app-pg-dumpall.html) for more information about them.


### ??? Mention configuration settings such as wal_size, etc. to make inserts into the target database faster
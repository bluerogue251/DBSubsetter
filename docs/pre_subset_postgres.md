## Pre-Subset Instructions: PostgreSQL

### Load the empty table structure from your "origin" database into your "target":

```bash
# Dump out all postgres roles into a file called `roles.sql`
$ pg_dumpall --roles-only --host=<originHost> --port=<originPort> --username=<originUser> --database=<originDb> --file roles.sql

# Load `roles.sql` into your "target" database
$ psql --host <targetHost> --port <targetPort> --user <targetUser> --dbname <targetDb> --file roles.sql

# Dump out just the schema (no data) from your "origin" database into a file called `pre-data-dump.sql`
$ pg_dump --host <originHost> --port <originPort> --user <originUser> --dbname <originDbName> --section pre-data --file pre-data-dump.sql

# Load `pre-data-dump.sql` into your "target" database
$ psql --host <targetHost> --port <targetPort> --user <targetUser> --dbname <targetDb> --file pre-data-dump.sql
```

Foreign keys and indices are purposely excluded from this step. They will be added to the "target" database in a later step, after subsetting has finished. This makes inserts into your target database significantly faster for large datasets. Temporarily avoiding foreign keys also greatly simplifies DBSubsetter's logic for inserting data.

For users without superuser access to the "origin" database (for example, those using Amazon RDS), `pg_dumpall` may error out on Postgres versions < 10 when trying to dump out the roles. See [here](http://www.thatguyfromdelhi.com/2016/12/custom-pgdumpall-now-works-with-aws.html) for details.
In this case, either the porting over of roles to the "target" database must be done manually, or `pg_dump` can be run with the `--no-privileges` and `--no-owner` options when dumping out `pre-data-dump.sql` so that missing roles are ignored.

The above commands have been tested against `pg_dump` and `pg_dumpall` version 9.6.6. Commands might be slightly different if you are using a different version of these tools. See the [pg_dump docs](https://www.postgresql.org/docs/current/static/app-pgdump.html) and the [pg_dumpall docs](https://www.postgresql.org/docs/current/static/app-pg-dumpall.html) for more information.


### Optimize your "target" database for fast inserts (optional)

Consider running `ALTER TABLE <your_table> SET UNLOGGED;` on the "target" database for any tables you expect to be of significant size in your "target" database. See the relevant [Postgres docs](https://www.postgresql.org/docs/9.6/static/sql-createtable.html#SQL-CREATETABLE-UNLOGGED) for more information.
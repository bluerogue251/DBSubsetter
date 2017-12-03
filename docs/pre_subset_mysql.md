## Pre-Subset Instructions: MySQL

### Load the empty table structure from your "origin" database into your "target"

```bash
# Dump out just the schema (no data) from your "origin" database(s) into a file called `pre-data-dump.sql`
$ mysqldump --host <originHost> --port <originPort> --user <originUser> --no-data --all-databases > pre-data-dump.sql

# Load `pre-data-dump.sql` into your "target" database
$ mysql --host <targetHost> --port <targetPort> --user <targetUser> < pre-data-dump.sql
```

These instructions have been tested against `mysql` version 14.14 and `mysqldump` version 10.13. Commands might be slightly different if you are using a different version of these tools. See the [mysql docs](https://dev.mysql.com/doc/refman/5.7/en/mysql-commands.html) and the [mysqldump docs](https://dev.mysql.com/doc/refman/5.7/en/mysqldump.html) for more information.
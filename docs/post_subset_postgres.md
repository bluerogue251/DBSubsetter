## Post-Subset Instructions: PostgreSQL

Add all foreign key constraints, indices, etc. to your "target" database. This corrects for the things we purposely left out of the "pre-subsetting" step.

```bash
$ pg_dump --host <originHost> --port <originPort> --user <originUser> --dbname <originDb> --section post-data --format custom --file post-data-dump.pgdump

$ pg_restore --host <targetHost> --port <targetPort> --user <targetUser> --dbname <targetDb> --jobs <numCPUCoresOnTargetDbMachine> post-data-dump.pgdump

```

### Fix primary key sequences

Now that your "target" db has data in it, reset your primary key sequence values using [these instructions](https://wiki.postgresql.org/wiki/Fixing_Sequences)

### Reinstate table logging

If you previously ran `ALTER TABLE <your_table> SET UNLOGGED;` on any tables in your "target" database before subsetting, then revert those changes by running `ALTER TABLE <your_table> SET LOGGED;` for those tables.
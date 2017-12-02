## PostgreSQL Post-Subset Instructions

Add all foreign key constraints, indices, etc. to your "target" database. This corrects for the things we purposely left out of the "pre-subsetting" step.

### pg_dump or pg_dumpall?????
```sh
$ pg_dump --host <originHost> --port <originPort> --user <originUser> --dbname <originDbName> --section post-data --format custom --file post-data-dump.pgdump

$ pg_restore --host <targetHost> --port <targetPort> --user <targetUser> --dbname <targetDbName> --jobs <numCPUCoresOnTargetDbMachine> post-data-dump.pgdump

```

Additionally, now that your "target" db has data in it, reset your primary key sequence values using [these instructions](https://wiki.postgresql.org/wiki/Fixing_Sequences)
## Post-Subset Instructions: PostgreSQL

### Reinstate constraints and indices

This corrects your "target" database for the foreign keys and indices  that we purposely left out of the `pre-data-dump.sql` file during the pre-subsetting step.
```bash
# Dump out just constraint and index definitions from your "origin" database into a file called `post-data-dump.pg_dump`
$ pg_dump --host <originHost> --port <originPort> --user <originUser> --dbname <originDb> --section post-data --format custom --file post-data-dump.pgdump

# Load `post-data-dump.pgdump` into your "target" database
$ pg_restore --host <targetHost> --port <targetPort> --user <targetUser> --dbname <targetDb> --jobs <numCPUCoresOnTargetDbMachine> post-data-dump.pgdump

```

### Fix primary key sequences

Now that your "target" database has data in it, reset your primary key sequence values using [these instructions](https://wiki.postgresql.org/wiki/Fixing_Sequences).


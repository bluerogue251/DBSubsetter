#!/usr/bin/env bash

set -eou pipefail

host=$1
origin_db_name=$2
target_db_name=$3

/opt/mssql-tools/bin/sqlcmd -S "${host}" -U sa -P MsSqlServerLocal1 -Q "drop database if exists $target_db_name"
/opt/mssql-tools/bin/sqlcmd -S "${host}" -U sa -P MsSqlServerLocal1 -Q "DBCC clonedatabase ($origin_db_name, $target_db_name) with no_statistics, no_querystore"
/opt/mssql-tools/bin/sqlcmd -S "${host}" -U sa -P MsSqlServerLocal1 -Q "alter database $target_db_name set read_write"
/opt/mssql-tools/bin/sqlcmd -S "${host}" -U sa -P MsSqlServerLocal1 -d "${target_db_name}" -Q "EXEC sp_msforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT all'"

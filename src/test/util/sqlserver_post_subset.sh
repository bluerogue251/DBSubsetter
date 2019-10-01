#!/usr/bin/env bash

set -eou pipefail

host=$1
target_db_name=$2

/opt/mssql-tools/bin/sqlcmd -S ${host} -U sa -P MsSqlServerLocal1 -d ${target_db_name} -Q "EXEC sp_msforeachtable 'ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all'"

#!/usr/bin/env bash

set -eou pipefail

container_name=$1
target_db_name=$2

docker exec ${container_name} /opt/mssql-tools/bin/sqlcmd -U sa -P MsSqlServerLocal1 -d ${target_db_name} -Q "EXEC sp_msforeachtable 'ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all'"

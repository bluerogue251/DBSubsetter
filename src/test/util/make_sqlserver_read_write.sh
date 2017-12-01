#!/usr/bin/env bash

set -eou pipefail

container_name=$1
db_name=$2

docker exec ${container_name} /opt/mssql-tools/bin/sqlcmd -U sa -P MsSqlServerLocal1 -Q "alter database $db_name set read_write"

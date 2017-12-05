#!/usr/bin/env bash
set -eou pipefail

container_name=$1
db_name=$2
schema_name=$3

docker exec ${container_name} /opt/mssql-tools/bin/sqlcmd -U sa -P MsSqlServerLocal1 -d ${db_name} -Q "create schema [$schema_name];"
#!/usr/bin/env bash
set -eou pipefail

host=$1
db_name=$2
schema_name=$3

/opt/mssql-tools/bin/sqlcmd -S ${host} -U sa -P MsSqlServerLocal1 -d ${db_name} -Q "create schema [$schema_name];"
#!/usr/bin/env bash

set -eou pipefail

host=$1
db_name=$2

/opt/mssql-tools/bin/sqlcmd -S ${host} -U sa -P MsSqlServerLocal1 -Q "create database [$db_name];"
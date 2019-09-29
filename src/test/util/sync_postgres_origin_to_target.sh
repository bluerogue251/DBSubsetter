#!/usr/bin/env bash

set -eou pipefail

origin_container=$1
origin_db=$2
target_container=$3
target_db=$4

pg_dump --host postgres --port 5432 --user postgres --section=pre-data ${origin_db} | psql --host postgres --port 5432 --user postgres ${target_db}

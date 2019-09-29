#!/usr/bin/env bash

set -eou pipefail

origin_host=$1
origin_port=$2
origin_db=$3
target_host=$4
target_port=$5
target_db=$6

pg_dump --host ${origin_host} --port ${origin_port} --user postgres --section=pre-data ${origin_db} | \
  psql --host ${target_host} --port ${target_port} --user postgres ${target_db}

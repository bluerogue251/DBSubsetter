#!/usr/bin/env bash

set -eou pipefail

origin_db_name=$1
origin_db_port=$2
target_db_name=$3
target_db_port=$4

pg_dump -h localhost -p $origin_db_port -U postgres --section=post-data $origin_db_name | psql -h localhost -p $target_db_port -U postgres $target_db_name -v ON_ERROR_STOP=1
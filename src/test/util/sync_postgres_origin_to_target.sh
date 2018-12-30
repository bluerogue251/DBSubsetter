#!/usr/bin/env bash

set -eou pipefail

origin_container=$1
origin_db=$2
target_container=$3
target_db=$4

docker exec ${origin_container} pg_dump --user postgres --section=pre-data ${origin_db} | docker exec -i ${target_container} psql --user postgres ${target_db}

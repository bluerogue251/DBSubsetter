#!/usr/bin/env bash

set -eou pipefail

database=$1
origin_container=$2
target_container=$3

docker exec ${origin_container} pg_dump -U postgres --section=post-data ${database} | docker exec -i ${target_container} psql -U postgres ${database} -v ON_ERROR_STOP=1
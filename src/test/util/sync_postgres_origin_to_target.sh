#!/usr/bin/env bash

set -eou pipefail

data_set_name=$1
origin_container=$2
target_container=$3

docker exec ${origin_container} pg_dump --user postgres --section=pre-data ${data_set_name} | docker exec -i ${target_container} psql --user postgres ${data_set_name}

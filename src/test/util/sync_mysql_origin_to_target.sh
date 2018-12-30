#!/usr/bin/env bash

set -eou pipefail

origin_container=$1
origin_database=$2
target_container=$3
target_database=$4

docker exec ${origin_container} mysqldump --user root --no-data ${origin_database} | docker exec -i ${target_container} mysql --user root ${target_database}

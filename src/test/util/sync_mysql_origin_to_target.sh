#!/usr/bin/env bash

set -eou pipefail

database=$1
origin_container=$2
target_container=$3

docker exec ${origin_container} mysqldump --user root --no-data ${database} | docker exec -i ${target_container} mysql --user root ${database}

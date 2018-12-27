#!/usr/bin/env bash

set -eou pipefail

data_set_name=$1
origin_container=$2
target_container=$3

docker exec ${origin_container} mysqldump --user root --no-data ${data_set_name} | docker exec -i ${target_container} mysql --user root ${data_set_name}

#!/usr/bin/env bash

set -eou pipefail

url=$1
container=$2
database=$3

wget -q -O - ${url} | gunzip | docker exec -i ${container} psql --user postgres --dbname ${database}
docker exec ${container} psql --user postgres --dbname ${database} -c "VACUUM ANALYZE"
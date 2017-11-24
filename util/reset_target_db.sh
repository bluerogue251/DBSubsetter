#!/usr/bin/env bash

set -eou pipefail

origin_db_name=$1
origin_db_port=$2
target_db_name=$3
target_db_port=$4

docker rm --force --volumes $target_db_name

docker create --name $target_db_name -p $target_db_port:5432 postgres:9.6.3

docker start $target_db_name

sleep 5

createdb -p $target_db_port -h localhost -U postgres $target_db_name

pg_dump -h localhost -p $origin_db_port -U postgres --section=pre-data $origin_db_name | psql -h localhost -p $target_db_port -U postgres $target_db_name -v ON_ERROR_STOP=1
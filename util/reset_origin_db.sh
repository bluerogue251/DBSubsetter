#!/usr/bin/env bash

set -eou pipefail

data_set_name=$1
origin_db_name=$2
origin_db_port=$3

docker rm --force --volumes $origin_db_name

docker create --name $origin_db_name -p $origin_db_port:5432 postgres:9.6.3

docker start $origin_db_name

sleep 5

createdb -p $origin_db_port -h localhost -U postgres $origin_db_name

psql -f ./util/$data_set_name/1_pre_data.sql -p $origin_db_port -h localhost -U postgres $origin_db_name -v ON_ERROR_STOP=1
psql -f ./util/$data_set_name/2_data.sql -p $origin_db_port -h localhost -U postgres $origin_db_name -v ON_ERROR_STOP=1
psql -f ./util/$data_set_name/3_post_data.sql -p $origin_db_port -h localhost -U postgres $origin_db_name -v ON_ERROR_STOP=1
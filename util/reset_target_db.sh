#!/usr/bin/env bash

set -eoux pipefail

data_set_name=$1
container_suffix=$2
origin_db_port=$3
target_db_port=$4

docker rm --force --volumes "$data_set_name"_target_${container_suffix} || true

docker create --name "$data_set_name"_target_${container_suffix} -p $target_db_port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0

docker start "$data_set_name"_target_${container_suffix}

sleep 15

mysql --port ${target_db_port} --host 0.0.0.0 --user root -e "create database $data_set_name"

mysqldump --host 0.0.0.0 --port $origin_db_port --user root --no-data $data_set_name | mysql --host 0.0.0.0 --port $target_db_port --user root $data_set_name
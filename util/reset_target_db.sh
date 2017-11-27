#!/usr/bin/env bash

set -eoux pipefail

origin_db_name=$1
origin_db_port=$2
target_db_name=$3
target_db_port=$4

docker rm --force --volumes $target_db_name || true

docker create --name $target_db_name -p $target_db_port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0

docker start $target_db_name

sleep 15

mysql --port $target_db_port --host 0.0.0.0 --user root -e "create database $target_db_name"

mysqldump --host 0.0.0.0 --port $origin_db_port --user root --no-data $origin_db_name | mysql --host 0.0.0.0 --port $target_db_port --user root $target_db_name
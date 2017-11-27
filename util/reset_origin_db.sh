#!/usr/bin/env bash

set -eoux pipefail

data_set_name=$1
origin_db_name=$2
origin_db_port=$3

docker rm --force --volumes $origin_db_name || true

docker create --name $origin_db_name -p $origin_db_port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0

docker start $origin_db_name

sleep 15

mysql --port $origin_db_port --host 0.0.0.0 --user root -e "create database $origin_db_name"
mysql --port $origin_db_port --host 0.0.0.0 --user root $origin_db_name < ./util/$data_set_name/1_pre_data.sql
mysql --port $origin_db_port --host 0.0.0.0 --user root $origin_db_name < ./util/$data_set_name/2_data.sql
mysql --port $origin_db_port --host 0.0.0.0 --user root $origin_db_name < ./util/$data_set_name/3_post_data.sql

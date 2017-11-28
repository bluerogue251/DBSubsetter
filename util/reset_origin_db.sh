#!/usr/bin/env bash

set -eoux pipefail

data_set_name=$1
origin_db_port=$2

docker rm --force --volumes "$data_set_name"_origin || true

docker create --name "$data_set_name"_origin -p $origin_db_port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0

docker start "$data_set_name"_origin

sleep 15

mysql --port $origin_db_port --host 0.0.0.0 --user root -e "create database $data_set_name"
#mysql --port $origin_db_port --host 0.0.0.0 --user root $data_set_name < ./util/$data_set_name/1_pre_data.sql
#mysql --port $origin_db_port --host 0.0.0.0 --user root $data_set_name < ./util/$data_set_name/2_data.sql
#mysql --port $origin_db_port --host 0.0.0.0 --user root $data_set_name < ./util/$data_set_name/3_post_data.sql

#psql -f ./util/$data_set_name/1_pre_data.sql -p $origin_db_port -h localhost -U postgres $origin_db_name -v ON_ERROR_STOP=1
#psql -f ./util/$data_set_name/2_data.sql -p $origin_db_port -h localhost -U postgres $origin_db_name -v ON_ERROR_STOP=1
#psql -f ./util/$data_set_name/3_post_data.sql -p $origin_db_port -h localhost -U postgres $origin_db_name -v ON_ERROR_STOP=1
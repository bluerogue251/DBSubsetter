#!/usr/bin/env bash

set -eou pipefail

data_set_name=$1
container=$2

docker exec ${container} mysql --user root -e "create database $data_set_name"
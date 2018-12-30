#!/usr/bin/env bash

set -eou pipefail

database=$1
container=$2

docker exec ${container} mysql --user root -e "create database ${database}"
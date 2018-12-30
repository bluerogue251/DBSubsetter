#!/usr/bin/env bash

set -eou pipefail

container=$1
database=$2

docker exec ${container} mysql --user root -e "create database ${database}"
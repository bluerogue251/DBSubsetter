#!/usr/bin/env bash

set -eou pipefail

host=$1
port=$2
database=$3

mysql --host "${host}" --port "${port}" --user root -e "drop database if exists ${database}"
mysql --host "${host}" --port "${port}" --user root -e "create database ${database}"

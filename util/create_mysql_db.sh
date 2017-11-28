#!/usr/bin/env bash

set -eoux pipefail

data_set_name=$1
port=$2

mysql --port ${port} --host 0.0.0.0 --user root -e "create database ${data_set_name}"
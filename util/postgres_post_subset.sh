#!/usr/bin/env bash

set -eou pipefail

data_set_name=$1
origin_port=$2
target_port=$3

pg_dump -h localhost -p ${origin_port} -U postgres --section=post-data ${data_set_name} | psql -h localhost -p ${target_port} -U postgres ${data_set_name} -v ON_ERROR_STOP=1
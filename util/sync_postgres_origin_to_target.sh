#!/usr/bin/env bash

set -eou pipefail

data_set_name=$1
origin_port=$2
target_port=$3

pg_dump --host 0.0.0.0 --port ${origin_port} --user postgres --section=pre-data ${data_set_name} | psql --host 0.0.0.0 --port ${target_port} --user postgres ${data_set_name}

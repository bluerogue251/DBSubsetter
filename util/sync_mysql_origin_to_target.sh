#!/usr/bin/env bash

set -eoux pipefail

data_set_name=$1
origin_port=$2
target_port=$3

mysqldump --host 0.0.0.0 --port ${origin_port} --user root --no-data ${data_set_name} | mysql --host 0.0.0.0 --port ${target_port} --user root ${data_set_name}

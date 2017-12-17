#!/usr/bin/env bash

set -eou pipefail


port=$1
data_set_name=$2

mysql --host 0.0.0.0 --port ${port} --user root ${data_set_name} < ./src/test/scala/e2e/mysqldatatypes/dml.sql
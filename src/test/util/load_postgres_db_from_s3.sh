#!/usr/bin/env bash

set -eou pipefail

dump_url=$1
host=$2
database=$3

wget -q -O - "${dump_url}" | gunzip | psql --host "${host}" --port 5432 --user postgres --dbname "${database}"
psql --host "${host}" --port 5432 --user postgres --dbname "${database}" -c "VACUUM ANALYZE"
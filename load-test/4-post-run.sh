#!/usr/bin/env bash

set -eou pipefail

# Runs after each load test

sudo docker exec pg_origin pg_dump --user postgres --dbname school_db --section post-data --format custom --file /tmp-data/school-db-post-data.pgdump
sudo docker exec pg_target pg_restore --user postgres --dbname school_db --jobs 8 /tmp-data/school-db-post-data.pgdump

sudo docker exec pg_origin pg_dump --user postgres --dbname physics_db --section post-data --format custom --file /tmp-data/physics-db-post-data.pgdump
sudo docker exec pg_target pg_restore --user postgres --dbname physics_db --jobs 8 /tmp-data/physics-db-post-data.pgdump

# Export prometheus metrics
# curl -X POST http://localhost:9090/api/v1/admin/tsdb/snapshot
# {
#  "status": "success",
#  "data": {
#    "name": "20171210T211224Z-2be650b6d019eb54"
#  }
#}
# The snapshot now exists at <data-dir>/snapshots/20171210T211224Z-2be650b6d019eb54
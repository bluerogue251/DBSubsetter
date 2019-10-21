#!/usr/bin/env bash

set -eou pipefail


sudo docker exec pg_origin pg_dump --user postgres --dbname school_db --section post-data --format custom --file /tmp-data/school-db-post-data.pgdump
sudo docker exec pg_target pg_restore --user postgres --dbname school_db --jobs 8 /tmp-data/school-db-post-data.pgdump

sudo docker exec pg_origin pg_dump --user postgres --dbname physics_db --section post-data --format custom --file /tmp-data/physics-db-post-data.pgdump
sudo docker exec pg_target pg_restore --user postgres --dbname physics_db --jobs 8 /tmp-data/physics-db-post-data.pgdump

# Export prometheus metrics
curl -X POST http://localhost:9090/api/v1/admin/tsdb/snapshot
# {
#  "status": "success",
#  "data": {
#    "name": "<snapshot-id>"
#  }
#}
# The snapshot now exists at /home/ubuntu/prometheus-data/snapshots/<snapshot-id>

# Zip up the prometheus snapshot
# zip -r <date>-prometheus-snapshot-<commit-hash>.zip prometheus-data/snapshots/<snapshot-id>

# On your local machine:
# scp -i "db-subsetter-load-test.pem" \
#   ubuntu@<ec2-host>:/home/ubuntu/<date>-prometheus-snapshot-<commit-hash>.zip \
#   <date>-prometheus-snapshot-<commit-hash>.zip

# Then manually save the prometheus snapshot data to the S3 db-subsetter/load-test/prometheus-snapshots bucket

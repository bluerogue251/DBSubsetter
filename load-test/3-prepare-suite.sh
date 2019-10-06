#!/usr/bin/env bash

set -eou pipefail

# Assumes the postgres data is preloaded into a volume mounted at /pg-origin-data
# lsblk # --> see what it's called and substitute into next command, maybe xvdb, maybe /dev/xvdf1, etc.
# sudo mount xvdb /pg-origin-data

DB_SUBSETTER_JAR_URL="https://s3.amazonaws.com/db-subsetter/load-test/jars/DBSubsetter-assembly-f91e64d0d622aeebf44f217e365f35ac990fd534.jar"
wget -O /home/ubuntu/DBSubsetter.jar "${DB_SUBSETTER_JAR_URL}"

sudo docker start pg_origin
sudo docker start pg_target
sudo docker start prometheus

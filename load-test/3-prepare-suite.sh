#!/usr/bin/env bash

set -eou pipefail

DB_SUBSETTER_JAR_URL="https://s3.amazonaws.com/db-subsetter/load-test/jars/DBSubsetter-assembly-f91e64d0d622aeebf44f217e365f35ac990fd534.jar"
wget -O /home/ubuntu/DBSubsetter.jar "${DB_SUBSETTER_JAR_URL}"

sudo docker start --detach pg_origin
sudo docker start --detach pg_target
sudo docker start --detach prometheus

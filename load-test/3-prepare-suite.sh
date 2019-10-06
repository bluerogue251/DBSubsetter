#!/usr/bin/env bash

set -eou pipefail

# Assumes the postgres data is preloaded into a volume mounted at /pg-origin-data
# lsblk # --> see what it's called and substitute into next command, maybe /dev/xvdb, maybe /dev/xvdf1, etc.
# sudo mount /dev/xvdb /pg-origin-data

sudo docker start pg_origin
sudo docker start pg_target
sudo docker start prometheus

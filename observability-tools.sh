#!/usr/bin/env bash

set -ou pipefail

docker rm --force --volumes db_subsetter_grafana
docker rm --force --volumes db_subsetter_prometheus
#docker rm --force --volumes db_subsetter_prometheus_pushgateway

docker run \
  --detach \
  --publish 3000:3000 \
  --name db_subsetter_grafana \
  grafana/grafana:5.4.2

docker run \
  --detach \
  --network host \
  --name db_subsetter_prometheus \
  --volume $(pwd)/prometheus-config.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:v2.6.0
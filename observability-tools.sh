#!/usr/bin/env bash

set -ou pipefail

docker rm --force --volumes db_subsetter_prometheus
docker rm --force --volumes db_subsetter_grafana

set -e

# TODO try to avoid using --network host
docker run \
  --detach \
  --network host \
  --name db_subsetter_prometheus \
  --volume $(pwd)/prometheus-config.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:v2.6.0
  # Can download a point-in-time snapshot of what previous load testing metrics were from S3, then:
  # unzip -r /path/to/<snapshot-id>
  # chmod 777 -R /path/to/<snapshot-id>
  # --volume /path/to/<snapshot-id>/:/prometheus \

# TODO try to avoid using --network host
docker run \
  --detach \
  --network host \
  --name db_subsetter_grafana \
  grafana/grafana:5.4.2

sleep 5

curl \
  -X POST \
  -H 'Content-Type: application/json' \
  --data '{"name": "prometheus", "type": "prometheus", "url": "http://localhost:9090", "access": "browser", "jsonData": { "timeInterval": "500ms" } }' \
  admin:admin@localhost:3000/api/datasources

echo ""

curl \
  -X POST \
  -H 'Content-Type: application/json' \
  --data @grafana-dashboard.json \
  admin:admin@localhost:3000/api/dashboards/db

echo ""

curl \
  -X PUT \
  -H 'Content-Type: application/json' \
  --data '{ "homeDashboardId": 1 }' \
  admin:admin@localhost:3000/api/user/preferences

echo ""
echo ""

echo "You can now view your DBSubsetter metrics at http://localhost:3000 (user: admin, password: admin)"
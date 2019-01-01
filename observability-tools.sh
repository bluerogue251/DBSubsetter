#!/usr/bin/env bash

set -ou pipefail

docker rm --force --volumes db_subsetter_grafana
docker rm --force --volumes db_subsetter_prometheus

docker run \
  --detach \
  --network host \
  --name db_subsetter_grafana \
  grafana/grafana:5.4.2

docker run \
  --detach \
  --network host \
  --name db_subsetter_prometheus \
  --volume $(pwd)/prometheus-config.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:v2.6.0

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
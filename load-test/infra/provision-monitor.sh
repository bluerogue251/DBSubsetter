#!/bin/bash

set -eou pipefail

#
# Script will be run from: /var/lib/cloud/instances/<instance-id>/
# Output for debugging goes to: /var/log/cloud-init-output.log
#

#
# Attach EBS Volume
# See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-using-volumes.html
#
mkfs -t xfs /dev/nvme1n1
mkdir /load-test
mount /dev/nvme1n1 /load-test
chmod 777 /load-test

#
# Install Prometheus
#
wget --quiet -O /load-test/prometheus.tar.gz https://github.com/prometheus/prometheus/releases/download/v2.21.0/prometheus-2.21.0.linux-amd64.tar.gz
tar xzf /load-test/prometheus.tar.gz --directory=/load-test
rm /load-test/prometheus.tar.gz

#
# Configure Prometheus
#
cat <<EOF >/load-test/prometheus.yml
global:
  scrape_interval: 500ms

scrape_configs:
  - job_name: db_subsetter
    static_configs:
      - targets: ['${pg-target-ip}:9092']
EOF

#
# Start prometheus
# https://prometheus.io/docs/introduction/first_steps/
#
/load-test/prometheus-2.21.0.linux-amd64/prometheus --config.file=/load-test/prometheus.yml &


#
# Install Grafana
#
wget --quiet -O /load-test/grafana.tar.gz https://dl.grafana.com/oss/release/grafana-7.2.1.linux-amd64.tar.gz
tar xzf /load-test/grafana.tar.gz --directory=/load-test
rm /load-test/grafana.tar.gz
wget --quiet -O /load-test/grafana-dashboard.json https://raw.githubusercontent.com/bluerogue251/DBSubsetter/master/grafana-dashboard.json

#
# Configure Grafana
#
cat <<EOF >/load-test/grafana.ini
[auth.anonymous]
enabled = true
org_role = Admin
EOF

#
# Start Grafana
#
/load-test/grafana-7.2.1/bin/grafana-server --homepath /load-test/grafana-7.2.1/ --config=/load-test/grafana.ini &
sleep 5

#
# Connect Grafana to Prometheus
#
curl \
  -X POST \
  -H 'Content-Type: application/json' \
  --data '{"name": "prometheus", "type": "prometheus", "url": "http://localhost:9090", "access": "proxy", "jsonData": { "timeInterval": "500ms" } }' \
  admin:admin@localhost:3000/api/datasources

#
# Create Grafana Dashboard
#
curl \
  -X POST \
  -H 'Content-Type: application/json' \
  --data @/load-test/grafana-dashboard.json \
  admin:admin@localhost:3000/api/dashboards/db

#
# Set Dashboard as Grafana Homepage
#
curl \
  -X PUT \
  -H 'Content-Type: application/json' \
  --data '{ "homeDashboardId": 1 }' \
  admin:admin@localhost:3000/api/org/preferences

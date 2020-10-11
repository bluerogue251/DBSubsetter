#!/bin/bash

set -eou pipefail

#
# Script will be run from: /var/lib/cloud/instances/<instance-id>/
# Output for debugging goes to: less /var/log/cloud-init-output.log
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
# Install Postgres
#
sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
apt update
apt install -y postgresql-10
pg_ctlcluster 10 main stop
pg_dropcluster 10 main

#
# Create load-test cluster
#
cat << EOF > /load-test/pg_hba.conf
local    all    all                      peer
host     all    all         0.0.0.0/0    md5
host     all    all         ::/0         md5
EOF
pg_createcluster 10 loadtest --datadir=/load-test/pg-data
pg_ctlcluster 10 loadtest start -o "-c listen_addresses=* -c hba_file=/load-test/pg_hba.conf -c shared_buffers=4GB -c work_mem=32MB -c maintenance_work_mem=2GB -c full_page_writes=off -c autovacuum=off -c wal_buffers=16MB -c max_wal_size=4GB"
sudo -u postgres psql -c "create role loadtest login superuser encrypted password 'load-test-pw'"

wget --quiet -O /load-test/school-db.sql.gz https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz
gunzip /load-test/school-db.sql.gz
sudo -u postgres createdb school_db
sudo -u postgres psql --dbname school_db < /load-test/school-db.sql
rm /load-test/school-db.sql

wget -O /load-test/physics-db-dump.tar "https://s3.amazonaws.com/db-subsetter/load-test/physics-db/physics-db-dump.tar"
tar -xf /load-test/physics-db-dump.tar --directory /load-test
sudo -u postgres createdb physics_db
sudo -u postgres pg_restore --verbose --jobs 4 --dbname physics_db /load-test/physics-db-dump
rm /load-test/physics-db-dump.tar
rm -rf /load-test/physics-db-dump

sudo -u postgres psql -c 'vacuum analyze'
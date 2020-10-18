#!/bin/bash

set -eou pipefail

#
# See https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/user-data.html
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
cat <<EOF >/load-test/pg_hba.conf
local    all    all                      peer
host     all    all         0.0.0.0/0    md5
host     all    all         ::/0         md5
EOF
pg_createcluster 10 loadtest --datadir=/load-test/pg-data
# http://www.databasesoup.com/2014/09/settings-for-fast-pgrestore.html
pg_ctlcluster 10 loadtest start -o "-c listen_addresses=* -c hba_file=/load-test/pg_hba.conf -c shared_buffers=2GB -c maintenance_work_mem=2GB -c fsync=off -c synchronous_commit=off -c wal_level=minimal -c full_page_writes=off -c wal_buffers=64MB -c max_wal_size=2GB -c max_wal_senders=0 -c wal_keep_segments=0 -c archive_mode=off -c autovacuum=off"
sudo -u postgres psql -c "create role loadtest login superuser encrypted password 'load-test-pw'"

#
# Load school_db origin dataset
#
wget --quiet -O /load-test/school-db.sql.gz https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz
gunzip /load-test/school-db.sql.gz
sudo -u postgres createdb school_db
time sudo -u postgres psql --dbname school_db < /load-test/school-db.sql
rm /load-test/school-db.sql
time sudo -u postgres psql --dbname school_db -c 'analyze'

#
# Start school_db load test (run starts upon detecting this DB)
#
sudo -u postgres createdb school_db_ready

#
# Await school_db load test completion
#
while ! sudo -u postgres --dbname school_db_complete -c "select 1" &> /dev/null
do
    echo "$(date) - waiting for school_db load test to complete"
    sleep 10
done

#
# Load physics_db origin dataset
#
wget --quiet -O /load-test/physics-db-z1.dump "https://s3.amazonaws.com/db-subsetter/load-test/physics-db/physics-db-z1.dump"
sudo -u postgres createdb physics_db
time sudo -u postgres pg_restore --exit-on-error --verbose --jobs 4 --dbname physics_db /load-test/physics-db-z1.dump
rm -rf /load-test/physics-db-z1.dump
time sudo -u postgres psql --dbname physics_db -c 'analyze'

#
# Start physics_db load test (run starts upon detecting this DB)
#
sudo -u postgres createdb physics_db_ready
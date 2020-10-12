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

wget --quiet -O /load-test/school-db.sql.gz https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz
gunzip /load-test/school-db.sql.gz
sudo -u postgres createdb school_db
time sudo -u postgres psql --dbname school_db < /load-test/school-db.sql
rm /load-test/school-db.sql
time sudo -u postgres psql --dbname school_db -c 'analyze'

wget --quiet -O /load-test/physics-db-z1.dump "https://s3.amazonaws.com/db-subsetter/load-test/physics-db/physics-db-z1.dump"
sudo -u postgres createdb physics_db
time sudo -u postgres pg_restore --exit-on-error --verbose --jobs 4 --dbname physics_db /load-test/physics-db-z1.dump
rm -rf /load-test/physics-db-z1.dump
time sudo -u postgres psql --dbname physics_db -c 'analyze'

#
# Monitor remotely with:
# PGPASSWORD=load-test-pw psql --user loadtest --dbname postgres --host <origin-db-host> -c "select state, query from pg_stat_activity where state != 'idle'"
#
# To create a new dump from this database:
# (TODO check compression level)
# time sudo -u postgres pg_dump --verbose --format custom --compress 0 --dbname physics_db --file /dump/physics-db-0.dump &> /dump/z0.log &
# time sudo -u postgres pg_dump --verbose --format custom --compress 1 --dbname physics_db --file /dump/physics-db-1.dump &> /dump/z1.log &
#


#time sudo -u postgres pg_dump --verbose --format custom --compress 0 --dbname school_db --file /dump/school-db-0.dump
#real0m13.985s
#user0m1.319s
#sys0m1.519s
#
#time sudo -u postgres pg_dump --verbose --format custom --compress 1 --dbname school_db --file /dump/school-db-1.dump
#real0m14.495s
#user0m8.213s
#sys0m0.452s
#
#time sudo -u postgres pg_dump --verbose --format custom --compress 6 --dbname school_db --file /dump/school-db-6.dump
#real0m20.316s
#user0m19.257s
#sys0m0.447s
#
#time sudo -u postgres pg_dump --verbose --format custom --compress 9 --dbname school_db --file /dump/school-db-9.dump
#real0m53.869s
#user0m52.725s
#sys0m0.554s
#
#-rw-rw-r--  1 postgres postgres 922M Oct 12 12:06 school-db-0.dump
#-rw-rw-r--  1 postgres postgres  77M Oct 12 12:05 school-db-1.dump
#-rw-rw-r--  1 postgres postgres  58M Oct 12 12:13 school-db-6.dump
#-rw-rw-r--  1 postgres postgres  55M Oct 12 12:08 school-db-9.dump

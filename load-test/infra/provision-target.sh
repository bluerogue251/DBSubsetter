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
pg_ctlcluster 10 loadtest start -o "-c listen_addresses=* -c hba_file=/load-test/pg_hba.conf -c shared_buffers=1GB -c maintenance_work_mem=1GB -c autovacuum=off"
sudo -u postgres psql -c "create role loadtest login superuser encrypted password 'load-test-pw'"

#
# Install Java
#
wget --quiet -O /load-test/jdk8.tar.gz https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u265-b01/OpenJDK8U-jdk_x64_linux_hotspot_8u265b01.tar.gz
tar xzf /load-test/jdk8.tar.gz --directory=/load-test
mv /load-test/jdk8u265-b01 /load-test/jdk8
rm /load-test/jdk8.tar.gz

#
# Install SBT
#
wget --quiet -O /load-test/sbt.tgz https://github.com/sbt/sbt/releases/download/v1.3.4/sbt-1.3.4.tgz
tar xzf /load-test/sbt.tgz --directory=/load-test
rm /load-test/sbt.tgz

#
# Build DBSubsetter
#
wget --quiet -O /load-test/DBSubsetter.tar.gz https://github.com/bluerogue251/DBSubsetter/archive/b5d7570.tar.gz
tar xzf /load-test/DBSubsetter.tar.gz --directory=/load-test
rm /load-test/DBSubsetter.tar.gz
cd /load-test/DBSubsetter-*
./../sbt/bin/sbt --java-home /load-test/jdk8 'set assemblyOutputPath in assembly := new File("/load-test/DBSubsetter.jar")' assembly

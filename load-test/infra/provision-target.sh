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

#
# Install Postgres
#
sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
apt update
apt install -y postgresql-10
cat << EOF > /home/ubuntu/pg_hba.conf
local    all    all                      peer
host     all    all         0.0.0.0/0    md5
host     all    all         ::/0         md5
EOF
pg_ctlcluster 10 main restart -o "-c listen_addresses=* -c hba_file=/home/ubuntu/pg_hba.conf"
sudo -u postgres psql -c "create role loadtest login superuser encrypted password 'load-test-pw'"

#
# Install Java
#
wget --quiet -O /home/ubuntu/jdk8.tar.gz https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u265-b01/OpenJDK8U-jdk_x64_linux_hotspot_8u265b01.tar.gz
tar xzf /home/ubuntu/jdk8.tar.gz --directory=/home/ubuntu
mv /home/ubuntu/jdk8u265-b01 /home/ubuntu/jdk8

#
# Install SBT
#
wget --quiet -O /home/ubuntu/sbt.tgz https://github.com/sbt/sbt/releases/download/v1.3.4/sbt-1.3.4.tgz
tar xzf /home/ubuntu/sbt.tgz --directory=/home/ubuntu

#
# Build DBSubsetter
#
wget --quiet -O /home/ubuntu/DBSubsetter.tar.gz https://github.com/bluerogue251/DBSubsetter/archive/aba1435.tar.gz
tar xzf /home/ubuntu/DBSubsetter.tar.gz --directory=/home/ubuntu
cd /home/ubuntu/DBSubsetter-*; ./sbt/bin/sbt --java-home /home/ubuntu/jdk8 assembly; cd -

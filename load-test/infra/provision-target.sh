#!/bin/bash

set -eou pipefail

#
# To debug this shell script:
# less /var/log/cloud-init-output.log
#

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
tar xzf /home/ubuntu/jdk8.tar.gz /home/ubuntu/jdk8.tar.gz --directory=/home/ubuntu
mv /home/ubuntu/jdk8u265-b01-jdk /home/ubuntu/jdk8

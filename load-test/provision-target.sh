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
build_jar() {
  echo "Building DBSubsetter-$1.jar"
  wget --quiet -O /load-test/DBSubsetter-"$1".tar.gz https://github.com/bluerogue251/DBSubsetter/archive/"$1".tar.gz
  wget --quiet -O /load-test/DBSubsetter-"$1".tar.gz https://github.com/bluerogue251/DBSubsetter/archive/"$1".tar.gz
  tar xzf /load-test/DBSubsetter-"$1".tar.gz --directory=/load-test
  rm /load-test/DBSubsetter-"$1".tar.gz
  cd /load-test/DBSubsetter-"$1"*
  ./../sbt/bin/sbt --java-home /load-test/jdk8 'set assemblyOutputPath in assembly := new File("/load-test/DBSubsetter-'"$1"'.jar")' assembly
}
build_jar "${old-commit}"
build_jar "${new-commit}"


#
# Wait for origin school_db to be ready
#
while ! PGPASSWORD=load-test-pw psql --host "${pg-origin-ip}" --user loadtest --dbname school_db_ready -c "select 1" &> /dev/null
do
    echo "$(date) - waiting for school_db origin to be ready"
    sleep 10
done

#
# Prepare school_db load test
#
PGPASSWORD=load-test-pw pg_dump \
  --host "${pg-origin-ip}" \
  --user loadtest \
  --dbname school_db \
  --section pre-data \
  > /load-test/school-db-pre.sql

PGPASSWORD=load-test-pw pg_dump \
  --host "${pg-origin-ip}" \
  --user loadtest \
  --dbname school_db \
  --section post-data \
  --format custom \
  > /load-test/school-db-post.pgdump

run_school_db_load_test() {
  sudo -u postgres dropdb --if-exists school_db
  sudo -u postgres createdb school_db
  sudo -u postgres psql --quiet --dbname school_db < /load-test/school-db-pre.sql
  echo "Running school_db load test ($1)"
  /load-test/jdk8/bin/java -Xmx2G -jar /load-test/DBSubsetter-"$1".jar \
    --originDbConnStr "jdbc:postgresql://${pg-origin-ip}:5432/school_db?user=loadtest&password=load-test-pw" \
    --targetDbConnStr "jdbc:postgresql://localhost:5432/school_db?user=loadtest&password=load-test-pw" \
    --keyCalculationDbConnectionCount 6 \
    --dataCopyDbConnectionCount 6 \
    --schemas "school_db,Audit" \
    --baseQuery "school_db.Students ::: student_id % 25 = 0 ::: includeChildren" \
    --baseQuery "school_db.standalone_table ::: id < 4 ::: includeChildren" \
    --excludeColumns "school_db.schools(mascot)" \
    --excludeTable "school_db.empty_table_2" \
    --exposeMetrics
  sudo -u postgres pg_restore --dbname school_db /load-test/school-db-post.pgdump
  sleep 15
}

#
# Run school_db load test three times for each commit
#
run_school_db_load_test "${old-commit}"
run_school_db_load_test "${new-commit}"
sleep 120
run_school_db_load_test "${old-commit}"
run_school_db_load_test "${new-commit}"
sleep 120
run_school_db_load_test "${old-commit}"
run_school_db_load_test "${new-commit}"

#
# Signal school_db load test is complete
#
PGPASSWORD=load-test-pw psql \
  --host "${pg-origin-ip}" \
  --user loadtest \
  --dbname school_db \
  -c "drop database if exists school_db_complete"
PGPASSWORD=load-test-pw psql \
  --host "${pg-origin-ip}" \
  --user loadtest \
  --dbname school_db \
  -c "create database school_db_complete"

#
# Wait for origin physics_db to be ready
#
while ! PGPASSWORD=load-test-pw psql --host "${pg-origin-ip}" --user loadtest --dbname physics_db_ready -c "select 1" &> /dev/null
do
    echo "$(date) - waiting for physics_db origin to be ready"
    sleep 300
done

#
# Prepare physics_db load test
#
PGPASSWORD=load-test-pw pg_dump \
  --host "${pg-origin-ip}" \
  --user loadtest \
  --dbname physics_db \
  --section pre-data \
  > /load-test/physics-db-pre.sql

PGPASSWORD=load-test-pw pg_dump \
  --host "${pg-origin-ip}" \
  --user loadtest \
  --dbname physics_db \
  --section post-data \
  --format custom \
  > /load-test/physics-db-post.pgdump

run_physics_db_load_test() {
  sudo -u postgres dropdb --if-exists physics_db
  sudo -u postgres createdb physics_db
  sudo -u postgres psql --quiet --dbname physics_db < /load-test/physics-db-pre.sql
  echo "Running physics_db load test ($1)"
  /load-test/jdk8/bin/java -Xmx2G -jar /load-test/DBSubsetter-"$1".jar \
    --originDbConnStr "jdbc:postgresql://${pg-origin-ip}:5432/physics_db?user=loadtest&password=load-test-pw" \
    --targetDbConnStr "jdbc:postgresql://localhost:5432/physics_db?user=loadtest&password=load-test-pw" \
    --keyCalculationDbConnectionCount 6 \
    --dataCopyDbConnectionCount 6 \
    --schemas "public" \
    --baseQuery "public.scientists ::: id in (2) ::: includeChildren" \
    --exposeMetrics
  sudo -u postgres pg_restore --dbname physics_db /load-test/physics-db-post.pgdump
}

#
# Run physics_db load test three times for each commit
#
run_physics_db_load_test "${old-commit}"
run_physics_db_load_test "${new-commit}"
run_physics_db_load_test "${old-commit}"
run_physics_db_load_test "${new-commit}"
run_physics_db_load_test "${old-commit}"
run_physics_db_load_test "${new-commit}"

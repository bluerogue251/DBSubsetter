#!/bin/bash

#
# Dependencies:
#
# 1) Docker installed locally
#    https://docs.docker.com/get-docker/
#
# 2) AWS account credentials stored at ~/.aws/credentials
#    https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html
#
# Input Arguments:
# $1 The git commit hash of the old code to use as the baseline for comparisons
# $2 The git commit hash of the new code to test and compare to the baseline
#

echo "Creating SSH Key (if not exists)"
echo "n" | ssh-keygen -q -N "" -f load-test/load-test.pem -C "load-test@example.com"
printf "\n\n"

set -eou pipefail

echo "Initializing Terraform"
./load-test/init.sh
printf "\n\n"

echo "Spinning up load test infrastructure"
./load-test/apply.sh "$1" "$2"

printf "\n\n"

echo "View results on the monitor instance's Grafana at http://<monitor-ip>:3000"

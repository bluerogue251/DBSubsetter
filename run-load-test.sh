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

echo "Creating SSH Key (if not exists)"
echo "n" | ssh-keygen -q -N "" -f load-test/infra/load-test.pem -C "load-test@example.com"
printf "\n\n"

set -eou pipefail

echo "Initializing Terraform"
./load-test/infra/init.sh
printf "\n\n"

echo "Spinning up load test infrastructure"
./load-test/infra/apply.sh
printf "\n\n"

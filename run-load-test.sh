#!/bin/bash

echo "Creating SSH Key (if not exists)"
echo "n" | ssh-keygen -q -N "" -f load-test/infra/load-test.pem -C "load-test@example.com"
printf "\n\n"

set -eou pipefail

echo "Initializing Terraform"
./load-test/infra/init.sh
printf "\n\n"

echo "Spinning up AWS infrastructure"
./load-test/infra/apply.sh
printf "\n\n"

#!/bin/bash

ssh-keygen -q -N "" -f load-test/load-test.pem -C "load-test@example.com"

./load-test/infra/init.sh
./load-test/infra/apply.sh
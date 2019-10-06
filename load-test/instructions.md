# Load Test Instructions

## Running up a load test

* There is a pre-existing AMI with dependencies and some test data pre-installed.
  Spin up an EC2 instance from this AMI (Amazon Machine Image) ID ami-026d4599c1d66c647.
  For consistency, use a "General Purpose" `t2.large` instance type (2 vCPUs, 8GB RAM)
* Build an uber jar of the DBSubsetter application locally with: 
  `$sbt 'set test in assembly := {}' assembly`
* `$ scp` the uber jar from your machine into the running EC2 instance
* Run script number 3 as a one-time setup step
* Run scripts numbers 4 and 5 as many times as desired

## Viewing metrics from the load test

* Edit observability-tools.sh in the project root directory to point Grafana to the 
  prometheus instance running on the EC2 instance itself. Then run ./observability-tools.sh
  and follow the printed instructions to open Grafana locally and view metrics from the load
  test running on the EC2 instance.

## Changing how load tests work

See scripts numbers 1 and 2 in this directory for manual setup for new or modified AMIs.
After running these scripts (with whatever changes you want), create the new AMI in the AWS browser UI.
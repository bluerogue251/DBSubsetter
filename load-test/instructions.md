# Load Test Instructions

## Running up a load test

* There is a pre-existing AMI with dependencies and some test data pre-installed.
  Spin up an EC2 instance from this AMI (Amazon Machine Image) ID ami-026d4599c1d66c647.
  For consistency, use a "General Purpose" `t2.large` instance type (2 vCPUs, 8GB RAM).
  Increase the root volume size to 100GB. This is where target data will end up.
  This should come with an external volume of 200GB. This is where origin data comes from. Leave that one as-is.
* Build an uber jar of the DBSubsetter application locally with: 
  `$ sbt 'set test in assembly := {}' assembly`
* `$ scp` the uber jar from your machine into the running EC2 instance
* Run script number 3 as a one-time setup step
* Run scripts numbers 4 and 5 as many times as desired (the second run onward tends to be most deterministic performance wise)

## Viewing metrics from the load test

* Make sure your EC2 instance's security group allows incoming traffic to port 9090,
  so that the prometheus instance running there is accessible to the outside world.
  
* Edit observability-tools.sh in the project root directory to point Grafana to the 
  prometheus instance running on the EC2 instance itself.
  
* Run ./observability-tools.sh and follow the printed instructions to open Grafana locally and view metrics from the load
  test running on the EC2 instance.
  
* Eventually we should have publicly available snapshots of prometheus data showing what baseline
  performance looks like, so we can evaluate whether the given changes are improvements
  or deteriorations.

## Changing how load tests work

See scripts numbers 1 and 2 in this directory for manual setup for new or modified AMIs.
After running these scripts (with whatever changes you want), create the new AMI in the AWS browser UI.

# Load Test Instructions

## Launch Amazon EC2 Instance

* Select AMI (Amazon Machine Image):
   - Name: db-subsetter-load-test
   - AMI ID: ami-026d4599c1d66c647
* Select instance type:
   - Family: General Purpose
   - Type: t2.large
   - vCPUs: 2
   - Memory: 8 GB
* Configure disks:
   - Increase root volume size to 100 GB. (Where target data will end up).
   - Verify an existing external volume of 200 GB. (Where origin data is)
* Select an existing security group:
   - Name: db-subsetter-load-test
   - Security Group ID: sg-0febc138089257766
   - Description: SSH and Prometheus Access
* Choose an existing key pair:
   - Name: db-subsetter-load-test
   - Private key file: db-subsetter.load-test.pem   
   
   
## Set up DBSubsetter on the new EC2 instance
   
* Build an uber jar of the DBSubsetter application locally: 
  ```
    $ sbt 'set test in assembly := {}; assemblyJarName in assembly := "latest.jar"' clean assembly
  ```
  
* Copy local resources onto the running EC2 instance:
  ```
  $ scp \
      -i ~/code/db-subsetter-load-test.pem \
      target/scala-2.12/latest.jar \
      load-test/4-run.sh \
      load-test/5-post-run.sh \
      ubuntu@ec2-3-230-142-113.compute-1.amazonaws.com:~
  ```

* Manually run the contents of 3-prepare-suite.sh
  ```
  $ ssh -i ~/code/db-subsetter-load-test.pem ubuntu@ec2-3-230-142-113.compute-1.amazonaws.com
  $ # ... then copy-paste individual commands from 3-prepare-suite.sh
  ```
  
* Run the load test multiple times (the second run onward has more deterministic performance)
  ```
  $ # Still on the remote EC2 instance
  $ chmod +x 4-run.sh 
  $ chmod +x 5-post-run.sh
  $ ./4-run.sh
  $ # ... and wait
  $ ./5-post-run.sh
  ```


## Viewing metrics from the load test

* Make sure your EC2 instance's security group allows incoming traffic to port 9090,
  so that the prometheus instance running there is accessible to the outside world.
  
* Edit observability-tools.sh in the project root directory to point Grafana to the 
  prometheus instance running on the EC2 instance itself.
  
* Run ./observability-tools.sh and follow the printed instructions to open Grafana locally and view metrics from the load
  test running on the EC2 instance.
  
* Eventually we should have publicly available snapshots of prometheus data showing what baseline
  performance looks like, so we can evaluate whether a given changes is an improvement or deterioration.

## Changing how load tests work

See scripts numbers 1 and 2 in this directory for manual setup for new or modified AMIs.
After running these scripts (with whatever changes you want), create the new AMI in the AWS browser UI.

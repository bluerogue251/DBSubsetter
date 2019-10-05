FROM openjdk:8-jdk-stretch

RUN apt-get update
RUN apt-get upgrade -y

RUN apt-get install -y postgresql-client

RUN apt-get install -y mysql-client

# Install some pre-requisites to HTTPS usage in certain apt functions
RUN apt-get install -y apt-transport-https ca-certificates

# Install SQL Server Tools
RUN curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add -
RUN curl https://packages.microsoft.com/config/ubuntu/16.04/prod.list | tee /etc/apt/sources.list.d/msprod.list
RUN apt-get update
ENV ACCEPT_EULA Y
RUN apt-get install -y mssql-tools unixodbc-dev

# Install SBT
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add
RUN apt-get update
RUN apt-get install -y sbt

# Install AWS CLI
RUN apt-get install -y python
RUN curl "https://s3.amazonaws.com/aws-cli/awscli-bundle.zip" -o "awscli-bundle.zip"
RUN unzip awscli-bundle.zip
RUN ./awscli-bundle/install -i /usr/local/aws -b /usr/local/bin/aws

# https://github.com/Microsoft/mssql-docker/issues/163
RUN apt-get install -y locales
RUN echo "nb_NO.UTF-8 UTF-8" > /etc/locale.gen
RUN echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen
RUN locale-gen

# Eagerly prime SBT and download project dependencies to speed up builds on CI
ADD . /tmp-project-install
WORKDIR /tmp-project-install
RUN sbt compile
WORKDIR root
run rm -rf /tmp-project-install
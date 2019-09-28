FROM openjdk:8-jdk-alpine

RUN apk add --no-cache bash

RUN apk add --no-cache --virtual=build-dependencies curl

RUN curl -sL "https://piccolo.link/sbt-1.0.4.tgz" | gunzip | tar -x -C /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt && \
    chmod 0755 /usr/local/bin/sbt && \
    apk del build-dependencies

# Eagerly prime SBT and download project dependencies to speed up builds on CI
ADD . /tmp-project-install
WORKDIR /tmp-project-install
RUN sbt compile
WORKDIR root
run rm -rf /tmp-project-install

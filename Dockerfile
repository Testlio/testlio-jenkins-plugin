FROM maven:3.8.6-openjdk-18
USER root

COPY . /usr/src/main
WORKDIR /usr/src/main

RUN mvn -X install
RUN mvn hpi:run -Dport=5002
FROM maven:3.8.6-eclipse-temurin-11
LABEL maintainer="dev@testlio.com"

ENV HOME=/home/java

USER root

RUN mkdir -p $HOME/app
WORKDIR $HOME/app

COPY . $HOME/app

RUN mvn install -DskipTests

ENTRYPOINT ["mvn"]
CMD ["hpi:run -Dhost=localhost -Dport=5002"]

EXPOSE 5002

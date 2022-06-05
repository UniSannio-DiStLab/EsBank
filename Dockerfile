FROM maven:3.8.5-jdk-11

#SHELL ["/bin/bash", "-c"]

ARG DEBIAN_FRONTEND=noninteractive
ENV WILDFLY_VERSION 25.0.1.Final
ENV WILDFLY_APP Bank

WORKDIR /opt

RUN curl -L -O https://github.com/wildfly/wildfly/releases/download/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
    && tar xf wildfly-$WILDFLY_VERSION.tar.gz \
    && rm wildfly-$WILDFLY_VERSION.tar.gz
RUN mv wildfly-${WILDFLY_VERSION} wildfly
RUN /opt/wildfly/bin/add-user.sh -u 'admin' -p 'assd2022'

RUN mkdir -p /opt/wildfly/modules/system/layers/base/com/mysql/main
WORKDIR /opt/wildfly/modules/system/layers/base/com/mysql/main

ADD module.xml .
RUN curl -O https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.20/mysql-connector-java-8.0.20.jar

RUN mkdir -p /source

WORKDIR /source

COPY . /source/

RUN cp standalone.conf /opt/wildfly/bin/standalone.conf
RUN cp standalone.xml /opt/wildfly/standalone/configuration/standalone.xml

RUN mvn clean install


RUN cp /source/target/${WILDFLY_APP}.war /opt/wildfly/standalone/deployments


EXPOSE 8080 8443 9990

ENTRYPOINT /opt/wildfly/bin/standalone.sh

#ENTRYPOINT sleep 100000
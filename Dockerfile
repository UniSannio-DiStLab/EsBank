FROM ubuntu:focal

SHELL ["/bin/bash", "-c"]

ARG DEBIAN_FRONTEND=noninteractive
ENV WILDFLY_USER wildfly
ENV WILDFLY_USER_PASSWORD wildfly
ENV USER_HOME = /home/$WILDFLY_USER
ENV WILDFLY_VERSION 25.0.1.Final
ENV WILDFLY_APP CurrencyConverter
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/

# Update system and install dependencies
USER root
RUN apt-get update
RUN apt-get -y install openjdk-11-jdk-headless
RUN apt-get -y install curl

# create application user
RUN useradd -m $WILDFLY_USER -p $WILDFLY_USER_PASSWORD
RUN usermod -aG sudo $WILDFLY_USER
RUN cp /root/.profile /home/wildfly/.profile
RUN cp /root/.bashrc /home/wildfly/.bashrc
RUN chown -R $WILDFLY_USER:$WILDFLY_USER /home/$WILDFLY_USER

USER $WILDFLY_USER

WORKDIR /home/$WILDFLY_USER

# wildfly installation
RUN curl -L -O https://github.com/wildfly/wildfly/releases/download/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
    && tar xf wildfly-$WILDFLY_VERSION.tar.gz \
    && rm wildfly-$WILDFLY_VERSION.tar.gz
COPY standalone.conf /home/${WILDFLY_USER}/wildfly-${WILDFLY_VERSION}/bin/standalone.conf
COPY standalone.xml /home/${WILDFLY_USER}/wildfly-${WILDFLY_VERSION}/standalone/configuration/standalone.xml
RUN  /home/${WILDFLY_USER}/wildfly-${WILDFLY_VERSION}/bin/add-user.sh -u 'admin' -p 'assd2022'

# maven installation
RUN curl -L -O https://dlcdn.apache.org/maven/maven-3/3.8.5/binaries/apache-maven-3.8.5-bin.tar.gz \
     && tar xzvf apache-maven-3.8.5-bin.tar.gz \
     && rm apache-maven-3.8.5-bin.tar.gz
ENV PATH=$PATH:/home/${WILDFLY_USER}/apache-maven-3.8.5/bin
RUN echo 'export PATH=$PATH:/home/${WILDFLY_USER}' >> ~/.bashrc

# Copy application source and configuration
COPY ./src /home/${WILDFLY_USER}/${WILDFLY_APP}/src
COPY ./pom.xml /home/${WILDFLY_USER}/${WILDFLY_APP}/
USER root
RUN chown -R $WILDFLY_USER:$WILDFLY_USER /home/$WILDFLY_USER
USER $WILDFLY_USER

# Compile application war
WORKDIR /home/$WILDFLY_USER/${WILDFLY_APP}
RUN mvn install

RUN cp /home/${WILDFLY_USER}/${WILDFLY_APP}/target/${WILDFLY_APP}.war /home/${WILDFLY_USER}/wildfly-${WILDFLY_VERSION}/standalone/deployments


# Expose the ports in which we're interested
EXPOSE 8080 8443 9990


#entrypoint sleep 1000
ENTRYPOINT /home/${WILDFLY_USER}/wildfly-${WILDFLY_VERSION}/bin/standalone.sh




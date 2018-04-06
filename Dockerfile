# The first part of this Dockerfile is inspired by an existing Dockerfile hosted at https://github.com/tthaomo/docker-scala-play
# The important parts have been copied over to remove a dependency on two public Docker containers
FROM anapsix/alpine-java:jdk8

ENV SBT_VERSION 0.13.15
ENV CHECKSUM 18b106d09b2874f2a538c6e1f6b20c565885b2a8051428bd6d630fb92c1c0f96

# Install sbt
RUN apk add --update bash curl openssl ca-certificates && \
    curl -L -o /tmp/sbt.zip https://dl.bintray.com/sbt/native-packages/sbt/${SBT_VERSION}/sbt-${SBT_VERSION}.zip && \
    openssl dgst -sha256 /tmp/sbt.zip | grep ${CHECKSUM} || (echo 'shasum mismatch' && false) && \
    mkdir -p /opt/sbt && \
    unzip /tmp/sbt.zip -d /opt/sbt && \
    rm /tmp/sbt.zip && \
    chmod +x /opt/sbt/sbt/bin/sbt && \
    ln -s /opt/sbt/sbt/bin/sbt /usr/bin/sbt && \
    rm -rf /tmp/* /var/cache/apk/*

WORKDIR /tmp/build

# Copy over the basic configuration files
COPY ["build.sbt", "/tmp/build/"]
COPY ["project/plugins.sbt", "project/sbt-ui.sbt", "project/build.properties", "/tmp/build/project/"]

# Sbt sometimes fails because of network problems. Retry 3 times.
RUN (sbt compile || sbt compile || sbt compile) && \
    (sbt test:compile || sbt test:compile || sbt test:compile) && \
    rm -rf /tmp/build

# Copy all of the code needed
COPY . /root/app/

# Copy the Docker hadatac.conf file over the original
COPY ./conf/hadatac-docker.conf /root/app/conf/hadatac.conf

# Change the working dir to the app to compile
WORKDIR /root/app

# Test compile the app (this can take a long time ~30 minutes or longer depending on the computer and its internet connection speed)
RUN sbt compile && sbt test:compile

# Expose the port the play app runs on
EXPOSE 9000

# Run the app when starting up the Docker container
ENTRYPOINT ["sbt"]
CMD ["run"]

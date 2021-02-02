# The first part of this Dockerfile is inspired by an existing Dockerfile hosted at https://github.com/mozilla/docker-sbt/blob/main/Dockerfile
# The important parts have been copied over to remove a dependency on two public Docker containers
FROM openjdk:11

ENV SBT_VERSION 1.3.10
#1.4.6
ENV CHECKSUM 86a6f28e79966074101066ab1aa19267d0c8d44eae21daa93c9c6056ca6f9da4

# Install sbt
#RUN apk add --update --no-cache ca-certificates curl git openssl && \
#    curl -L -o /tmp/sbt.zip https://dl.bintray.com/sbt/debian/sbt/${SBT_VERSION}/sbt-${SBT_VERSION}.zip && \
#    openssl dgst -sha256 /tmp/sbt.zip | grep ${CHECKSUM} || (echo 'shasum mismatch' && false) && \
#    mkdir -p /opt/sbt && \
#    unzip /tmp/sbt.zip -d /opt/sbt && \
#    rm /tmp/sbt.zip && \
#    chmod +x /opt/sbt/sbt/bin/sbt && \
#    ln -s /opt/sbt/sbt/bin/sbt /usr/bin/sbt && \
#    rm -rf /tmp/* /var/cache/apk/* && \
#    apk del ca-certificates curl openssl
# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt rpm -y

WORKDIR /tmp/build

# Copy over the basic configuration files
COPY ["build.sbt", "/tmp/build/"]
COPY ["project/plugins.sbt", "project/sbt-ui.sbt", "project/build.properties", "/tmp/build/project/"]

# Sbt sometimes fails because of network problems. Retry 3 times.
RUN (sbt compile || sbt compile || sbt compile) && \
    (sbt test:compile || sbt test:compile || sbt test:compile) && \
    rm -rf /tmp/build

# Copy all of the code needed
COPY . /root/app2/

# Copy the Docker hadatac.conf file over the original
COPY ./conf/hadatac-docker.conf /root/app2/conf/hadatac.conf

# Copy the Docker version of the autoccsv.config file over
COPY ./conf/autoccsv-docker.config /root/app2/conf/autoccsv.config

# Create the csv folders so we can copy data into them right away
RUN mkdir -p /root/app2/csvs/processed_csv /root/app2/csvs/unprocessed_csv /root/app2/csvs/downloaded_csv

# Declare the conf and csvs volumes.
# Any changes made to files in these directories (by this Dockerfile) after this command will be discarded
#   see: https://docs.docker.com/engine/reference/builder/#notes-about-specifying-volumes
# This is written like this so that the copied-over config files in previous
#   two COPY commands will not be overwritten by the following COPY command.
VOLUME ["/root/app2/conf", "/root/app2/csvs"]


# Change the working dir to the app to compile
WORKDIR /root/app2

# Test compile the app (this can take a long time ~30 minutes or longer depending on the computer and its internet connection speed)
RUN sbt compile && sbt test:compile

# Expose the port the play app runs on
EXPOSE 9000

# Run the app when starting up the Docker container
ENTRYPOINT ["sbt"]
CMD ["run"]

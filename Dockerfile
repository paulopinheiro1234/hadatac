# The first part of this Dockerfile is inspired by an existing Dockerfile hosted at https://github.com/mozilla/docker-sbt/blob/main/Dockerfile
# The important parts have been copied over to remove a dependency on two public Docker containers
#FROM openjdk:11
FROM hseeberger/scala-sbt:11.0.12_1.5.5_2.12.14

ENV JAVA_OPTS="-Xms6048m -Xmx10000m"
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

# Copy the Docker version of the autoccsv.config file over
COPY ./conf/autoccsv-docker.config /root/app/conf/autoccsv.config

# Create the csv folders so we can copy data into them right away
RUN mkdir -p /root/app/csvs/processed_csv /root/app/csvs/unprocessed_csv /root/app/csvs/downloaded_csv

# Declare the conf and csvs volumes.
# Any changes made to files in these directories (by this Dockerfile) after this command will be discarded
#   see: https://docs.docker.com/engine/reference/builder/#notes-about-specifying-volumes
# This is written like this so that the copied-over config files in previous
#   two COPY commands will not be overwritten by the following COPY command.
VOLUME ["/root/app/conf", "/root/app/csvs"]


# Change the working dir to the app to compile
WORKDIR /root/app

# Test compile the app (this can take a long time ~30 minutes or longer depending on the computer and its internet connection speed)
RUN sbt compile && sbt test:compile

# Expose the port the play app runs on
EXPOSE 9000
EXPOSE 8080
EXPOSE 8983

# Run the app when starting up the Docker container
ENTRYPOINT ["sbt"]
CMD ["run"]

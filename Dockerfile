# The first part of this Dockerfile is inspired by an existing Dockerfile hosted at https://github.com/mozilla/docker-sbt/blob/main/Dockerfile
# The important parts have been copied over to remove a dependency on two public Docker containers
#FROM openjdk:11
FROM hseeberger/scala-sbt:11.0.13_1.5.6_2.12.15 as build-java

RUN apt-get update && apt-get install -y unzip
ENV JAVA_OPTS="-Xms6048m -Xmx10000m"
WORKDIR /hadatac

# Copy over the basic configuration files
#COPY ["build.sbt", "/tmp/build/"]
#COPY ["project/plugins.sbt", "project/sbt-ui.sbt", "project/build.properties", "/tmp/build/project/"]

# Sbt sometimes fails because of network problems. Retry 3 times.
#RUN (sbt compile || sbt compile || sbt compile) && \
#    (sbt test:compile || sbt test:compile || sbt test:compile) && \
#    rm -rf /tmp/build

COPY . /hadatac

RUN sbt playUpdateSecret && sbt dist
RUN cd /hadatac/target/universal/ && unzip hadatac-10.0.1-SNAPSHOT.zip

FROM openjdk:11-jre-slim

WORKDIR /hadatac

COPY --from=build-java /hadatac/target/universal/hadatac-10.0.1-SNAPSHOT /hadatac

COPY ./conf/hadatac-docker.conf /hadatac/conf/hadatac.conf
COPY ./conf/autoccsv-docker.config /hadatac/conf/autoccsv.config

EXPOSE 9000

ENTRYPOINT [ "bin/hadatac" ]

FROM solr:8.11.1

#### Installs JTS Topology Suite 1.14

# Change directory so that our commands run inside this new directory
WORKDIR /opt/solr/server

USER ${user}
# Downloads and unzips JTS library & moves JTS Libs to SOLR Libs and deletes intermediate files and directories
# Tries to download it three times before failing. Sometimes on slow networks, the download fails.
RUN wget -O jts-1.15.jar https://repo1.maven.org/maven2/org/locationtech/jts/jts-core/1.15.0/jts-core-1.15.0.jar \
    && cp jts-1.15.jar solr-webapp/webapp/WEB-INF/lib/

### /JTS Install

### Copy the schemas from solr-home to the mycores directory.
# Perform the copy to the mycores directory. All cores here get created automatically.
COPY ./solr-home /opt/solr/server/solr/mycores
COPY ./solr-home/solr.xml /opt/solr/server/solr/mycores/solr.xml
COPY ./D* /opt/solr/server/solr/
COPY ./solr8* /opt/solr/server/solr/
COPY ./LICENSE /opt/solr/server/solr/LICENSE
COPY ./.dockerignore /opt/solr/server/solr/.dockerignore

# Make the solr user the owner of these files
USER root
RUN chown -R solr:solr /opt/solr/server/solr/mycores
ENV SOLR_HOME=/opt/solr/server/solr/mycores/
RUN cd /opt/solr/server/solr/mycores/ ; echo $(pwd) ; echo $(ls -lrt)
#### /Copy Schemas

# Change back to the solr user
USER solr

EXPOSE 8983

# Set solr-foreground as the final command. This is the default already.
CMD ["solr-foreground"]
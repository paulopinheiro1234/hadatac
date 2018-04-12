FROM solr:6.5

#### Installs JTS Topology Suite 1.14

# Change directory so that our commands run inside this new directory
WORKDIR /opt/solr/server

# Unzips JTS library & moves JTS Libs to SOLR Libs and deletes intermediate files and directories
RUN wget https://sourceforge.net/projects/jts-topo-suite/files/jts/1.14/jts-1.14.zip \
    && unzip -o jts-1.14.zip -d jtsio \
    && cp -r jtsio/lib/. solr-webapp/webapp/WEB-INF/lib \
    && rm -rf jts-1.14.zip ./jtsio

#### /JTS Install

#### Copy the schemas from solr-home to the mycores directory.
# Perform the copy to the mycores directory. All cores here get created automatically.
# COPY ./solr-home /opt/solr/server/solr/mycores
#
# # Make the solr user the owner of these files
# USER root
# RUN chown -R solr:solr /opt/solr/server/solr/mycores
# #### /Copy Schemas
# 
# # Change back to the solr user
# USER solr

COPY hadatac_solr.dj.zip /opt/solr/server/hadatac_solr.zip

# Unzips SOLR seed data & copies SOLR Seed data to correct director
RUN unzip hadatac_solr.zip && cp -r hadatac_solr/. solr && rm -f hadatac_solr.zip


# Set solr-foreground as the final command. This is the default already.
CMD ["solr-foreground"]

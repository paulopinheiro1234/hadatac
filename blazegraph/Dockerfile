FROM nawer/blazegraph:2.1.4

# Copy and import store and store_users namespaces
# COPY store.properties /tmp/import/store.properties
#
# RUN java -cp /usr/bin/blazegraph.jar com.bigdata.rdf.store.DataLoader -quiet -namespace store /tmp/import/store.properties
#
# COPY store_users.properties /tmp/import/store_users.properties
#
# RUN java -cp /usr/bin/blazegraph.jar com.bigdata.rdf.store.DataLoader -quiet -namespace store_users /tmp/import/store_users.properties
#
# RUN rm -rf /tmp/import

# Copies Blazegraph Seed ZIP
COPY blazegraph.zip /var/lib/blazegraph/blazegraph_data.zip

WORKDIR /var/lib/blazegraph

# unzips Blazegraph Seed & removes Blazegraph Seed data zip file & installs blazegraph.jar
RUN unzip blazegraph_data.zip && rm blazegraph_data.zip

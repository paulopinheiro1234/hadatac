FROM appropriate/curl:latest

COPY store.properties /store.properties
COPY store_users.properties /store_users.properties

# Adds the 'store' and 'store_users' namespaces to Blazegraph
CMD curl -X POST --data-binary @store.properties -H 'Content-Type:text/plain' blazegraph:9999/blazegraph/namespace && \
    curl -X POST --data-binary @store_users.properties -H 'Content-Type:text/plain' blazegraph:9999/blazegraph/namespace

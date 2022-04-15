FROM atomgraph/fuseki

RUN mkdir /fuseki/configuration
COPY fuseki-jetty.xml hadatac_assembler.ttl /fuseki/configuration/

CMD [ \
    "--config=/fuseki/configuration/hadatac_assembler.ttl", \
    "--jetty=/fuseki/configuration/fuseki-jetty.xml", \
    "--ping", \
    "--stats", \
    "--metrics", \
    "--compact" \
    ]

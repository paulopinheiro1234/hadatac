FROM 1science/sbt

# # # # #

# Change directory so that our commands run inside this new directory
WORKDIR /data

# Clones latest HADATAC codebase
RUN wget https://github.com/paulopinheiro1234/hadatac/archive/master.zip

# Unzips HADATAC source
RUN unzip master.zip

# Copies hadatac.conf
COPY ./conf/hadatac.conf /data/hadatac-master/conf/hadatac.conf

# Copies labkey.config
COPY ./conf/labkey.config /data/hadatac-master/conf/labkey.config

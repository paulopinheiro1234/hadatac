#!/bin/bash
clear
echo "=== HADataC - The Human-Aware Data Acquisition Framework - Deployment Script ==="
echo ""

# Make sure only root can run this script
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

read -r -p "Proceed with deployment? [y/N] " response
case $response in
    [yY][eE][sS]|[yY]) 
        ;;
    *)
        exit
        ;;
esac

HADATAC_HOST=$(hostname --long)

case $HADATAC_HOST in
    "chear.tw.rpi.edu")
        SOLR_HOME="/data/hadatac-solr/solr" 
        GIT_HOME="/data/hadatac" ;;
    "chear-test.tw.rpi.edu")
        SOLR_HOME="/data/hadatac-solr/solr"  
        GIT_HOME="/data/git/hadatac";;
    "case.tw.rpi.edu")
        SOLR_HOME="/var/hadatac/solr"  
        GIT_HOME="/data/git/hadatac";;
    *)
        SOLR_HOME="/var/hadatac/solr"  
        GIT_HOME="/data/git/hadatac";;
esac

echo "Deploying $HADATAC_HOST"
echo ""
echo "Stopping Hadatac Service"
echo ""
service hadatac stop
echo "Stopping Solr5 Service"
echo ""
sh ${SOLR_HOME}/run_solr5.sh stop
echo ""
echo "Stopping Blazegraph Service"
echo ""
service jetty8 stop
echo ""
echo "Changing to Hadatac Git Directory"
echo ""
cd ${GIT_HOME}
echo ""
echo "Checking out Latest Code from GitHub"
echo ""
git checkout -- conf/hadatac.conf
git checkout -- conf/labkey.config
git checkout -- conf/namespaces.properties
git checkout -- conf/play-authenticate/smtp.conf conf/play-authenticate/
git pull
echo ""
echo "Creating Distribution File"
echo ""
sbt clean
sbt dist
echo ""
echo "Copy Distribution File to /data directory"
echo ""
cp ${GIT_HOME}/target/universal/hadatac-1.0-SNAPSHOT.zip /data/
echo ""
echo "Removing Old Distribution Folder"
echo ""
rm -rf /data/hadatac-1.0-SNAPSHOT
cd /data/
echo ""
echo "Unzipping Current Distribution File"
echo ""
unzip hadatac-1.0-SNAPSHOT.zip
echo ""
echo "Copying over config files"
echo ""
cd /data/hadatac-1.0-SNAPSHOT
cp /data/conf/hadatac.conf conf/
cp /data/conf/labkey.config conf/
cp /data/conf/namespaces.properties conf/
cp /data/conf/play-authenticate/smtp.conf conf/play-authenticate/
echo ""
echo "Starting Services"
echo ""
sh ${SOLR_HOME}/run_solr5.sh start
service jetty8 start 
service hadatac start
echo ""
echo "Deployment Complete"
echo ""

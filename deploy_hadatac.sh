#!/bin/bash
clear
echo "=== HADataC - The Human-Aware Data Acquisition Framework - Deployment Script ==="
echo ""

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
echo "Stopping Blazegraph Service"
echo ""
service jetty8 stop
echo "Changing to Hadatac Git Directory"
echo ""
cd ${GIT_HOME}
echo "Checking out Latest Code from GitHub"
echo ""
git checkout -- conf/hadatac.conf
git checkout -- conf/labkey.config
git pull
echo "Copying over config files"
echo ""
cp /data/conf/hadatac.conf conf/
cp /data/conf/labkey.config conf/
cp /data/conf/play-authenticate/smtp.conf conf/play-authenticate/
rm -rf ~/.sbt/0.13
sbt clean
sbt dist
cp ${GIT_HOME}/target/universal/hadatac-1.0-SNAPSHOT.zip /data/
rm -rf /data/hadatac-1.0-SNAPSHOT
cd /data/
unzip hadatac-1.0-SNAPSHOT.zip 
sh ${SOLR_HOME}/run_solr5.sh start
service jetty8 start 
service hadatac start ;;

#case $HADATAC_HOST in
#    "chear.tw.rpi.edu")
#        echo "Deploying chear"
#        echo ""
#        echo "Stopping Hadatac Service"
#        echo ""
#        service hadatac stop
#        echo "Stopping Solr5 Service"
#        echo ""
#        sh /data/hadatac-solr/solr/run_solr5.sh stop
#        echo "Stopping Blazegraph Service"
#        echo ""
#        service jetty8 stop
#        echo "Changing to Hadatac Git Directory"
#        echo ""
#        cd /data/hadatac
#        echo "Checking out Latest Code from GitHub"
#        echo ""
#        git checkout -- conf/hadatac.conf
#        git checkout -- conf/labkey.config
#        git pull
#        echo "Copying over config files"
#        echo ""
#        cp /data/conf/hadatac.conf conf/
#        cp /data/conf/labkey.config conf/
#        cp /data/conf/play-authenticate/smtp.conf conf/play-authenticate/
#        rm -rf ~/.sbt/0.13
#        sbt clean
#        sbt dist
#        cp /data/hadatac/target/universal/hadatac-1.0-SNAPSHOT.zip /data/
#        rm -rf /data/hadatac-1.0-SNAPSHOT
#        cd /data/
#        unzip hadatac-1.0-SNAPSHOT.zip 
#        sh /data/hadatac-solr/solr/run_solr5.sh start
#        service jetty8 start 
#        service hadatac start ;;
#    "chear-test.tw.rpi.edu")
#        echo "Deploying chear-test"
#        echo ""
#        service hadatac stop
#        sh /data/hadatac-solr/solr/run_solr5.sh stop
#        service jetty8 stop
#        cd /data/git/hadatac
#        git checkout -- conf/hadatac.conf
#        git checkout -- conf/labkey.config
#        git pull
#        cp /data/conf/hadatac.conf conf/
#        cp /data/conf/labkey.config conf/
#        cp /data/conf/play-authenticate/smtp.conf conf/play-authenticate/
#        rm -rf ~/.sbt/0.13
#        sbt clean
#        sbt dist
#        cp /data/git/hadatac/target/universal/hadatac-1.0-SNAPSHOT.zip /data/
#        rm -rf /data/hadatac-1.0-SNAPSHOT
#        cd /data/
#        unzip hadatac-1.0-SNAPSHOT.zip 
#        sh /data/hadatac-solr/solr/run_solr5.sh start
#        service jetty8 start 
#        service hadatac start ;;
#    "case.tw.rpi.edu")
#        echo "Deploying case"
#        echo ""
#        service hadatac stop
#        sh /var/hadatac/solr/run_solr5.sh stop
#        service jetty8 stop
#        cd /data/git/hadatac
#        git checkout -- conf/hadatac.conf
#        git checkout -- conf/labkey.config
#        git pull
#        cp /data/conf/hadatac.conf conf/
#        cp /data/conf/labkey.config conf/
#        cp /data/conf/play-authenticate/smtp.conf conf/play-authenticate/
#        rm -rf ~/.sbt/0.13
#        sbt clean
#        sbt dist
#        cp /data/git/hadatac/target/universal/hadatac-1.0-SNAPSHOT.zip /data/
#        rm -rf /data/hadatac-1.0-SNAPSHOT
#        cd /data/
#        unzip hadatac-1.0-SNAPSHOT.zip 
#        sh /data/hadatac-solr/solr/run_solr5.sh start
#        service jetty8 start 
#        service hadatac start ;;
#    *)
#        echo "Not on a prespecified Production Machine, please follow local deployment procedures"
#        echo ""
#        echo $HADATAC_HOST ;;
#esac


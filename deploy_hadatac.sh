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
        echo "Deploying chear"
        echo ""
        service hadatac stop
        sh /data/hadatac-solr/solr/run_solr5.sh stop
        service jetty8 stop
        cd /data/hadatac
        git checkout -- conf/hadatac.conf
        git checkout -- conf/labkey.config
        git pull
        cp /data/conf/hadatac.conf conf/
        cp /data/conf/labkey.config conf/
        cp /data/conf/play-authenticate/smtp.conf conf/play-authenticate/
        rm -rf ~/.sbt/0.13
        sbt clean
        sbt dist
        cp /data/git/hadatac/target/universal/hadatac-1.0-SNAPSHOT.zip /data/
        rm -rf /data/hadatac-1.0-SNAPSHOT
        cd /data/
        unzip hadatac-1.0-SNAPSHOT.zip 
        sh /data/hadatac-solr/solr/run_solr5.sh start
        service jetty8 start 
        service hadatac start ;;
    "chear-test.tw.rpi.edu")
        echo "Deploying chear-test"
        echo ""
        service hadatac stop
        sh /data/hadatac-solr/solr/run_solr5.sh stop
        service jetty8 stop
        cd /data/git/hadatac
        git checkout -- conf/hadatac.conf
        git checkout -- conf/labkey.config
        git pull
        cp /data/conf/hadatac.conf conf/
        cp /data/conf/labkey.config conf/
        cp /data/conf/play-authenticate/smtp.conf conf/play-authenticate/
        rm -rf ~/.sbt/0.13
        sbt clean
        sbt dist
        cp /data/git/hadatac/target/universal/hadatac-1.0-SNAPSHOT.zip /data/
        rm -rf /data/hadatac-1.0-SNAPSHOT
        cd /data/
        unzip hadatac-1.0-SNAPSHOT.zip 
        sh /data/hadatac-solr/solr/run_solr5.sh start
        service jetty8 start 
        service hadatac start ;;
    "case.tw.rpi.edu")
        echo "Deploying case"
        echo ""
        service hadatac stop
        sh /var/hadatac/solr/run_solr5.sh stop
        service jetty8 stop
        cd /data/git/hadatac
        git checkout -- conf/hadatac.conf
        git checkout -- conf/labkey.config
        git pull
        cp /data/conf/hadatac.conf conf/
        cp /data/conf/labkey.config conf/
        cp /data/conf/play-authenticate/smtp.conf conf/play-authenticate/
        rm -rf ~/.sbt/0.13
        sbt clean
        sbt dist
        cp /data/git/hadatac/target/universal/hadatac-1.0-SNAPSHOT.zip /data/
        rm -rf /data/hadatac-1.0-SNAPSHOT
        cd /data/
        unzip hadatac-1.0-SNAPSHOT.zip 
        sh /data/hadatac-solr/solr/run_solr5.sh start
        service jetty8 start 
        service hadatac start ;;
    *)
        echo "Not on a prespecified Production Machine, please follow local deployment procedures"
        echo ""
        echo $HADATAC_HOST ;;
esac


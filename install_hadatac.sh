#!/bin/bash
clear
echo "=== Welcome to HADataC - The Human-Aware Data Collection Framework ==="
echo ""
echo "  The following wizard will guide you into deploying a working"
echo "instance of HADataC on your machine. Please refer to"
echo "https://github.com/paulopinheiro1234/hadatac/wiki if you have any"
echo "questions about this installation."
echo ""
echo "  ATTENTION:"
echo "  1) This script downloads and install Apache Solr. This takes around"
echo "     300Mbytes of data. Make sure you have a decent connection and"
echo "     this data availability."
echo ""

read -r -p "Proceed with installation? [y/N] " response
case $response in
    [yY][eE][sS]|[yY]) 
        ;;
    *)
        exit
        ;;
esac

echo ""
read -r -p "Directory of installation [~/hadatac]: " response
if [ "$response" == "" ]
then HADATAC_HOME=~/hadatac
else HADATAC_HOME=$response
fi

HADATAC_DOWNLOAD=$HADATAC_HOME/download
HADATAC_SOLR=$HADATAC_HOME/solr
SOLR4_HOME=$HADATAC_SOLR/solr-4.10.4
SOLR5_HOME=$HADATAC_SOLR/solr-5.2.1

mkdir $HADATAC_HOME
mkdir $HADATAC_DOWNLOAD
mkdir $HADATAC_SOLR

cp -R * $HADATAC_HOME

echo "=== Downloading Apache Solr 4.10.4..."
wget -O $HADATAC_DOWNLOAD/solr-4.10.4.tgz http://archive.apache.org/dist/lucene/solr/4.10.4/solr-4.10.4.tgz
wait $!
wget -O $HADATAC_DOWNLOAD/solr-4.10.4.tgz.md5 http://archive.apache.org/dist/lucene/solr/4.10.4/solr-4.10.4.tgz.md5
wait $!
echo "=== Downloading Apache Solr 5.2.1..."
wget -O $HADATAC_DOWNLOAD/solr-5.2.1.tgz http://archive.apache.org/dist/lucene/solr/5.2.1/solr-5.2.1.tgz
wait $!
wget -O $HADATAC_DOWNLOAD/solr-5.2.1.tgz.md5 http://archive.apache.org/dist/lucene/solr/5.2.1/solr-5.2.1.tgz.md5
wait $!
echo "=== Downloading JTS Topology Suite 1.13..."
wget -O $HADATAC_DOWNLOAD/jts-1.13.zip http://iweb.dl.sourceforge.net/project/jts-topo-suite/jts/1.13/jts-1.13.zip
wait $!

echo "=== Uncompressing Apache Solr 4.10.4..."
tar xfz $HADATAC_DOWNLOAD/solr-4.10.4.tgz -C $HADATAC_SOLR
wait $!
echo "=== Uncompressing Apache Solr 5.2.1..."
tar xfz $HADATAC_DOWNLOAD/solr-5.2.1.tgz -C $HADATAC_SOLR
wait $!
echo "=== Uncompressing JTS Topology Suite 1.13..."
unzip -o -qq $HADATAC_DOWNLOAD/jts-1.13.zip -d $HADATAC_DOWNLOAD/jts-1.13
wait $!

echo "HADATAC_SOLR=$HADATAC_SOLR" >> $HADATAC_SOLR/hadatac_solr.sh
cat $HADATAC_SOLR/solr4.in.sh >> $HADATAC_SOLR/hadatac_solr.sh
mv $HADATAC_SOLR/hadatac_solr.sh $HADATAC_SOLR/solr4.in.sh

echo "HADATAC_SOLR=$HADATAC_SOLR" >> $HADATAC_SOLR/hadatac_solr.sh
cat $HADATAC_SOLR/run_solr4.sh >> $HADATAC_SOLR/hadatac_solr.sh
mv $HADATAC_SOLR/hadatac_solr.sh $HADATAC_SOLR/run_solr4.sh

echo "HADATAC_SOLR=$HADATAC_SOLR" >> $HADATAC_SOLR/hadatac_solr.sh
cat $HADATAC_SOLR/solr5.in.sh >> $HADATAC_SOLR/hadatac_solr.sh
mv $HADATAC_SOLR/hadatac_solr.sh $HADATAC_SOLR/solr5.in.sh

echo "HADATAC_SOLR=$HADATAC_SOLR" >> $HADATAC_SOLR/hadatac_solr.sh
cat $HADATAC_SOLR/run_solr5.sh >> $HADATAC_SOLR/hadatac_solr.sh
mv $HADATAC_SOLR/hadatac_solr.sh $HADATAC_SOLR/run_solr5.sh

sh $HADATAC_SOLR/run_solr4.sh start
wait $!
sh $HADATAC_SOLR/run_solr5.sh start
wait $!

cp $HADATAC_DOWNLOAD/jts-1.13/lib/* $HADATAC_SOLR/solr-5.2.1/server/solr-webapp/webapp/WEB-INF/lib/

sh $HADATAC_SOLR/run_solr4.sh restart
sh $HADATAC_SOLR/run_solr5.sh restart





#!/bin/bash
clear
echo "=== Welcome to HADataC - The Human-Aware Data Acquisition Framework ==="
echo ""
echo "  The following wizard will guide you into deploying a working"
echo "instance of HADataC on your machine. Please refer to"
echo "https://github.com/paulopinheiro1234/hadatac/wiki if you have any"
echo "questions about this installation."
echo ""
echo "Note: This script can be automated by specifying install parameters."
echo "Usage: bash install_hadatac.sh [Y/N] [solr_install_dir] [blazegraph_install_dir]"
echo ""
echo "  ATTENTION:"
echo "  1) This script downloads and install Apache Solr. This takes around"
echo "     300Mbytes of data. Make sure you have a decent connection and"
echo "     this data availability."
echo ""

#dpkg -s zip &> /dev/null
zipDir=$(command -v zip)

if [ -z $zipDir ]

  then
    echo "  2) Program missing: 'zip' is required for installation"
    echo "     Please install zip before proceeding"
    echo ""
    exit
fi

if [ "$#" -gt 0 ]; then
  case $1 in
    [yY][eE][sS]|[yY])
        ;;
    *)
        exit
        ;;
  esac
else
  read -r -p "Proceed with installation? [y/N] " response
  case $response in
    [yY][eE][sS]|[yY])
        ;;
    *)
        exit
        ;;
  esac
fi
echo ""
if [ "$#" -gt 1 ]; then
  HADATAC_HOME=$2
else
  read -r -p "Directory of installation [~/hadatac-solr]: " response
  if [ "$response" == "" ]
  then HADATAC_HOME=~/hadatac-solr
  else HADATAC_HOME=$response
  fi
fi
echo ""
if [ "$#" -gt 2 ]; then
  BLAZEGRAPH_HOME=$3
else
  read -r -p "Directory of installation [~/hadatac-blazegraph]: " response
  if [ "$response" == "" ]
  then BLAZEGRAPH_HOME=~/hadatac-blazegraph
  else BLAZEGRAPH_HOME=$response
  fi
fi
HADATAC_DOWNLOAD=$HADATAC_HOME/download
HADATAC_SOLR=$HADATAC_HOME/solr
SOLR6_HOME=$HADATAC_SOLR/solr-6.5.0
JETTY_NAME=jetty-distribution-9.4.12.v20180830

mkdir $HADATAC_HOME
mkdir $HADATAC_DOWNLOAD
mkdir $HADATAC_SOLR
mkdir $BLAZEGRAPH_HOME

cp -R solr/ $HADATAC_SOLR

echo "=== Downloading Apache Solr 6.5.0..."
wget -O $HADATAC_DOWNLOAD/solr-6.5.0.tgz http://archive.apache.org/dist/lucene/solr/6.5.0/solr-6.5.0.tgz
wait $!
wget -O $HADATAC_DOWNLOAD/solr-6.5.0.tgz.md5 http://archive.apache.org/dist/lucene/solr/6.5.0/solr-6.5.0.tgz.md5
wait $!
echo "=== Downloading JTS Topology Suite 1.14..."
wget -O $HADATAC_DOWNLOAD/jts-1.14.zip https://sourceforge.net/projects/jts-topo-suite/files/jts/1.14/jts-1.14.zip

echo "=== Uncompressing Apache Solr 6.5.0..."
tar -xzf $HADATAC_DOWNLOAD/solr-6.5.0.tgz -C $HADATAC_SOLR
wait $!
echo "=== Uncompressing JTS Topology Suite 1.14..."
unzip -o -qq $HADATAC_DOWNLOAD/jts-1.14.zip -d $HADATAC_DOWNLOAD/jts-1.14
wait $!

mv $HADATAC_SOLR/solr/* $HADATAC_SOLR
rm -rf $HADATAC_SOLR/solr

echo "HADATAC_SOLR=$HADATAC_SOLR" >> $HADATAC_SOLR/hadatac_solr.sh
cat $HADATAC_SOLR/solr6.in.sh >> $HADATAC_SOLR/hadatac_solr.sh
mv $HADATAC_SOLR/hadatac_solr.sh $HADATAC_SOLR/solr6.in.sh

echo "HADATAC_SOLR=$HADATAC_SOLR" >> $HADATAC_SOLR/hadatac_solr.sh
cat $HADATAC_SOLR/run_solr6.sh >> $HADATAC_SOLR/hadatac_solr.sh
mv $HADATAC_SOLR/hadatac_solr.sh $HADATAC_SOLR/run_solr6.sh

sh $HADATAC_SOLR/run_solr6.sh stop
wait $!
sh $HADATAC_SOLR/run_solr6.sh start
wait $!

cp $HADATAC_DOWNLOAD/jts-1.14/lib/* $HADATAC_SOLR/solr-6.5.0/server/solr-webapp/webapp/WEB-INF/lib/

echo "=== Starting Apache Solr 6.5.0..."
sh $HADATAC_SOLR/run_solr6.sh restart

echo "=== Downloading Jetty ..."
wget -O $BLAZEGRAPH_HOME/$JETTY_NAME.zip https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.12.v20180830/jetty-distribution-9.4.12.v20180830.zip
wait $!
echo ""

unzip $BLAZEGRAPH_HOME/$JETTY_NAME.zip -d $BLAZEGRAPH_HOME/

echo "=== Downloading Blazegraph ..."
wget -O $BLAZEGRAPH_HOME/blazegraph.war https://sourceforge.net/projects/bigdata/files/bigdata/2.0.0/bigdata.war/download
wait $!
echo ""

mkdir $BLAZEGRAPH_HOME/$JETTY_NAME/webapps/blazegraph
mv $BLAZEGRAPH_HOME/blazegraph.war $BLAZEGRAPH_HOME/$JETTY_NAME/webapps/blazegraph/
unzip $BLAZEGRAPH_HOME/$JETTY_NAME/webapps/blazegraph/blazegraph.war -d $BLAZEGRAPH_HOME/$JETTY_NAME/webapps/blazegraph/

cp ./blazegraph/jetty-webapp.xml $BLAZEGRAPH_HOME/$JETTY_NAME/etc/

echo ""
echo "Copying over config files"
echo ""
mkdir -p ../conf/ && cp -pr ./conf/. ../conf/

cp -pr ../conf/. ./conf/

echo "=== Installation is finished ..."

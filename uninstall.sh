#!/bin/bash
clear
echo "=== Welcome to HADataC - The Human-Aware Data Acquisition Framework ==="
echo ""
echo "  The following wizard will guide you in uninstalling a working"
echo "instance of HADataC on your machine."

echo "Note: This script can be automated by specifying uninstall parameters."
echo "Usage: bash install_hadatac.sh [Y/N] [solr_install_dir] [blazegraph_install_dir]"


if [ "$#" -gt 0 ]; then
  case $1 in
    [yY][eE][sS]|[yY])
        ;;
    *)
        exit
        ;;
  esac
else
  read -r -p "Proceed with uninstall? [y/N] " response
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
SOLR8_HOME=$HADATAC_SOLR/solr-8.6.1
JETTY_NAME=jetty-distribution-9.4.12.v20180830

echo "=== Stopping Apache Solr 8.6.1..."
sh $HADATAC_SOLR/run_solr8.sh stop

echo "=== Deleting Apache Solr 8.6.1..."
rm -rf $HADATAC_SOLR

echo "=== Stopping Blazegraph..."
sh $BLAZEGRAPH_HOME/$JETTY_NAME/bin/jetty.sh stop

echo "=== Deleting Blazegraph..."
rm -rf $BLAZEGRAPH_HOME

echo "=== Uninstall is finished ..."

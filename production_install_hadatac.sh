#!/bin/bash
clear
echo "=== Welcome to HADataC - The Human-Aware Data Acquisition Framework ==="
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
read -r -p "Path of Data Folder [default: /data]: " response
if [ "$response" == "" ]
then DATA_HOME=/data
else DATA_HOME=$response
fi

echo ""
read -r -p "Directory of installation [default: /data/hadatac-solr]: " response
if [ "$response" == "" ]
then HADATAC_HOME=/data/hadatac-solr
else HADATAC_HOME=$response
fi

rm -rf $HADATAC_HOME
wait $!

HADATAC_DOWNLOAD=$HADATAC_HOME/download
HADATAC_SOLR=$HADATAC_HOME/solr
SOLR6_HOME=$HADATAC_SOLR/solr-6.5.0

mkdir $HADATAC_HOME
mkdir $HADATAC_DOWNLOAD
mkdir $HADATAC_SOLR

cp -R * $HADATAC_HOME
if [ ! -d "$DATA_HOME/conf" ]; then
  cp -R ./conf $DATA_HOME
fi

echo "=== Downloading Apache Solr 6.5.0..."
wget -O $HADATAC_DOWNLOAD/solr-6.5.0.tgz http://archive.apache.org/dist/lucene/solr/6.5.0/solr-6.5.0.tgz
wait $!
wget -O $HADATAC_DOWNLOAD/solr-6.5.0.tgz.md5 http://archive.apache.org/dist/lucene/solr/6.5.0/solr-6.5.0.tgz.md5
wait $!
echo "=== Downloading JTS Topology Suite 1.14..."
wget -O $HADATAC_DOWNLOAD/jts-1.14.zip https://sourceforge.net/projects/jts-topo-suite/files/jts/1.14/jts-1.14.zip

echo "=== Uncompressing Apache Solr 6.5.0..."
tar xfz $HADATAC_DOWNLOAD/solr-6.5.0.tgz -C $HADATAC_SOLR
wait $!
echo "=== Uncompressing JTS Topology Suite 1.14..."
unzip -o -qq $HADATAC_DOWNLOAD/jts-1.14.zip -d $HADATAC_DOWNLOAD/jts-1.14
wait $!

echo "HADATAC_SOLR=$HADATAC_SOLR" >> $HADATAC_SOLR/hadatac_solr.sh
cat $HADATAC_SOLR/solr6.in.sh >> $HADATAC_SOLR/hadatac_solr.sh
mv $HADATAC_SOLR/hadatac_solr.sh $HADATAC_SOLR/solr6.in.sh

echo "HADATAC_SOLR=$HADATAC_SOLR" >> $HADATAC_SOLR/hadatac_solr.sh
cat $HADATAC_SOLR/run_solr6.sh >> $HADATAC_SOLR/hadatac_solr.sh
mv $HADATAC_SOLR/hadatac_solr.sh $HADATAC_SOLR/run_solr6.sh

sh $HADATAC_SOLR/run_solr6.sh start
wait $!

cp $HADATAC_DOWNLOAD/jts-1.14/lib/* $HADATAC_SOLR/solr-6.5.0/server/solr-webapp/webapp/WEB-INF/lib/

sh $HADATAC_SOLR/run_solr6.sh restart

echo "=== Installing puppet..."
apt-get install puppet
wait $!

echo "=== Installing puppetlabs-stdlib --version 4.14.0..."
puppet module install puppetlabs-stdlib --version 4.14.0
wait $!

echo "=== Installing maestrodev-wget --version 1.7.3..."
puppet module install maestrodev-wget --version 1.7.3
wait $!

echo "=== Installing blazegraph using puppet..."
puppet apply blazegraph.pp
wait $!

echo "=== Creating store namespace..."
curl -X POST --data-binary @store.properties -H 'Content-Type:text/plain' http://localhost:8080/bigdata/namespace
wait $!
echo ""

echo "=== Creating store_users namespace..."
curl -X POST --data-binary @store_users.properties -H 'Content-Type:text/plain' http://localhost:8080/bigdata/namespace
wait $!
echo ""

echo "=== Installing hadatac service..."
cp hadatac.initd /etc/init.d/
mv /etc/init.d/hadatac.initd /etc/init.d/hadatac
chmod 755 /etc/init.d/hadatac
chown root:root /etc/init.d/hadatac
update-rc.d hadatac defaults 
wait $!
echo ""

echo ""
echo "Installation Complete"
echo ""

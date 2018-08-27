#!/bin/bash
clear
echo "=== HADataC - The Human-Aware Data Acquisition Framework - Set Domain Script ==="
echo ""

# Make sure only root can run this script
if [ "$(id -u)" != "0" ]; then
      echo "This script must be run as root" 1>&2
      exit 1
fi

# Make sure a domain name is provided
if [ $# -ne 1 ]; then
      echo "This script requires the acronym of the new domain" 1>&2
      exit 1
fi

echo "Requested domain: $1"

CONF_DIR="./conf"
HADATAC_CONF=$CONF_DIR/hadatac.conf
LABKEY=$CONF_DIR/labkey.config
NAMESPACES=$CONF_DIR/namespaces.properties
TEMPLATE=$CONF_DIR/templace.conf

echo ""
echo "testing " $HADATAC_CONF.$1
if [ -f $HADATAC_CONF.$1 ]; then
    echo "Copying $HADATAC_CONF.$1 to $HADATAC_CONF"
    cp -f $HADATAC_CONF.$1 $HADATAC_CONF
fi
if [ -f $LABKEY.$1 ]; then
    echo "Copying $LABKEY.$1 to $LABKEY"
    cp -f $LABKEY.$1 $LABKEY
fi
if [ -f $NAMESPACES.$1 ]; then
    echo "Copying $NAMESPACES.$1 to $NAMESPACES"
    cp -f $NAMESPACES.$1 $NAMESPACES
fi
if [ -f $TEMPLATE.$1 ]; then
    echo "Copying $TEMPLATE.$1 to $TEMPLATE"
    cp -f $TEMPLATE.$1 $TEMPLATE
fi
wait $!

echo "Domain has been set"
echo ""

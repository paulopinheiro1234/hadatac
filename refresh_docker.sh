#!/bin/bash
clear
echo "=== HADataC - The Human-Aware Data Acquisition Infrastructure - Refresh Docker Containers Script ==="
echo ""

# Make sure only root can run this script
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

docker-compose stop
wait $!

docker system prune -f
wait $!

docker volume prune -f
wait $!

docker-compose build
wait $!

docker-compose up



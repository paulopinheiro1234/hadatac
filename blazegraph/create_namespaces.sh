#/bin/sh

echo "Creating namepsaces..."

sleep 20s

cd /tmp/import

echo "=== Creating store namespace..."
curl -X POST --data-binary @store.properties -H 'Content-Type:text/plain' http://localhost:8080/blazegraph/namespace
echo ""

echo "=== Creating store_sandbox namespace..."
curl -X POST --data-binary @store_sandbox.properties -H 'Content-Type:text/plain' http://localhost:8080/blazegraph/namespace
echo ""

echo "=== Creating store_users namespace..."
curl -X POST --data-binary @store_users.properties -H 'Content-Type:text/plain' http://localhost:8080/blazegraph/namespace
echo ""

echo "=== Creating store namespace..."
curl -X POST --data-binary @blazegraph/store.properties -H 'Content-Type:text/plain' http://localhost:8080/blazegraph/namespace
echo ""

echo "=== Creating store_sandbox namespace..."
curl -X POST --data-binary @blazegraph/store_sandbox.properties -H 'Content-Type:text/plain' http://localhost:8080/blazegraph/namespace
echo ""

echo "=== Creating store_users namespace..."
curl -X POST --data-binary @blazegraph/store_users.properties -H 'Content-Type:text/plain' http://localhost:8080/blazegraph/namespace
echo ""

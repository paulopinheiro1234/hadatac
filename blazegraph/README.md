# HADatAc Blazegraph

## Running with Docker Compose
You can create a running Blazegraph instance with the correct configuration settings using Docker and Docker Compose now.


### Building and Starting the Service
From the parent directory (i.e. ./hadatac) run these commands in a shell for development:

```bash
# Build only the blazegraph service
docker-compose build blazegraph
# Start the blazegraph service in daemon mode
docker-compose up -d blazegraph
```

Now, if you access http://localhost:9999 you will see a running Blazegraph page with empty `kb`, `store` and `store_users` namespaces.

### Stopping the Service
To stop the service you should run `docker-compose stop blazegraph`.

This will stop the Docker container, but will persist the data that was ingested as described in the next section.

### Volume Data
A Docker Volume is defined for the Blazegraph service in the `docker-compose.yml` file. This is meant to persist the data when stopping and starting the service.

To find out where this data is stored on your local machine, run `docker volume ls` and find the `VOLUME NAME` of the volume. It will likely be called `hadatac_hadatac-blazegraph`.

When you have the volume name, type `docker inspect VOLUME_NAME`. It will list the mountpoint of the volume. You will need to be root to access this directory, but it contains all of the data added to the Blazegraph container during its existence.

### Tearing Down the Service

To completely remove the Docker Blazegraph service and its volume data, run `docker-compose down -v --remove-orphans`. To tear it down, but keep the volume data run `docker-compose down --remove-orphans`. Please note, that these commands will also remove the other services as well.

# HADatAc Blazegraph

## Running with Docker Compose
You can create a running Blazegraph instance with the correct configuration settings using Docker and Docker Compose now.

### Development Mode
From the parent directory (i.e. ./hadatac) run these commands in a shell for development:

```bash
# Create an alias for convenience
alias comp_dev='docker-compose -f docker-compose.yml -f docker-compose.dev.yml'
# Build only the solr service
comp_dev build blazegraph
# Start the solr service in daemon mode
comp_dev up -d blazegraph
```

Now, if you access http://localhost:9999 you will see a running Blazegraph page with empty `kb`, `store` and `store_users` namespaces.

### Production Mode
From the parent directory (i.e. ./hadatac) run these commands in a shell for development:

```bash
# Create an alias for convenience
alias comp='docker-compose'
# Build only the Blazegraph service
comp build blazegraph
# Start the Blazegraph service in daemon mode
comp up -d blazegraph
```

This will spin up the same Blazegraph instance, but the port in the container is not forwarded to the localhost for security.

### Stopping the Service
In either production or development mode, to stop the service you should run `comp stop blazegraph` or `comp_dev stop blazegraph`, respectively.

This will stop the Docker container, but will persist the data that was ingested as described in the next section.

### Volume Data
A Docker Volume is defined for the Blazegraph service in the `docker-compose.yml` file. This is meant to persist the data when stopping and starting the service.

To find out where this data is stored on your local machine, run `docker volume ls` and find the `VOLUME NAME` of the volume. It will likely be called `hadatac_hadatac-blazegraph`.

When you have the volume name, type `docker inspect VOLUME_NAME`. It will list the mountpoint of the volume. You will need to be root to access this directory, but it contains all of the schema attributes and any data that were added to the Solr container during its existence.

### Tearing Down the Service

To completely remove the Docker Compose Blazegraph service and its volume data, run `[comp_dev OR comp] down -v --remove-orphans`. To tear it down, but keep the volume data run `[comp_dev OR comp] down --remove-orphans`. This will also remove the other services and their data as well.

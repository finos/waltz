

# Run Waltz as a container

### Pre Requisites

* Docker
* Postgres DB instance (optional, can run in Docker instead)

# Configuration

- The Waltz container will use default values to connect its DB.
- By default it will try to `update` its DB and then `run` Waltz.
- You can change this by providing environment variables to the container on the command line or as part of [docker-compose.yml](../docker-compose.yml)

## Default values and actions
The container will execute two commands: `update` and `run`. The first command will `update` the database instance by running `liquibase` against it. The second command `run` will execute `catalina.sh run` to `run` Waltz.

The default parameters are listed below:

* `DB_HOST="postgres"`
* `DB_PORT="5432"`
* `DB_NAME="waltz"`
* `DB_USER="waltz"`
* `DB_PASSWORD="waltz"`
* `DB_SCHEME="waltz"`
* `WALTZ_FROM_EMAIL="help@finos.org"`
* `WALTZ_BASE_URL="http://127.0.0.1:8080/"`
* `CHANGELOG_FILE=_FILE="/opt/waltz/liquibase/db.changelog-master.xml"`

# Running

## Docker Compose
To start Waltz with a Postgres instance in just one command, you can use [docker-compose.yml](../docker-compose.yml) and run it with:

    $> docker-compose up

When the server starts you will see messages about registering
endpoints and CORS services, similar to:

````
....
waltz_1     | 16:33:53.088 [localhost-startStop-1] DEBUG o.f.w.w.e.a.StaticResourcesEndpoint - Registering static resources
waltz_1     | 16:33:53.089 [localhost-startStop-1] INFO  org.finos.waltz.web.Main - Completed endpoint registration
waltz_1     | 16:33:53.093 [localhost-startStop-1] INFO  org.finos.waltz.web.Main - GZIP not enabled
waltz_1     | 16:33:53.094 [localhost-startStop-1] INFO  org.finos.waltz.web.Main - Enabled CORS
waltz_1     | 09-Dec-2021 16:33:53.108 INFO [localhost-startStop-1] org.apache.catalina.startup.HostConfig.deployWAR Deployment of web application archive [/usr/local/tomcat/webapps/ROOT.war] has finished in [4,292] ms
waltz_1     | 09-Dec-2021 16:33:53.110 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler ["http-nio-8080"]
waltz_1     | 09-Dec-2021 16:33:53.117 INFO [main] org.apache.catalina.startup.Catalina.start Server startup in 4351 ms
````

Once the container is up you can access the Waltz dashboard on [http://127.0.0.1:8080/](http://127.0.0.1:8080/)

## Docker run

Run waltz without updating the database:

    $> docker run ghcr.io/finos/waltz \
      -p 8080:8080 \
      -e "DB_HOST=IP_or_FQDN" \
      -e "DB_PORT=5432" \
      -e "DB_NAME=demo" \
      -e "DB_USER=user" \
      -e "DB_PASSWORD=password" \
      run

Update the database and run Waltz with fresh database:

    $> docker run ghcr.io/finos/waltz \
      -p 8080:8080 \
      -e "DB_HOST=IP_or_FQDN" \
      -e "DB_PORT=5432" \
      -e "DB_NAME=demo" \
      -e "DB_USER=user" \
      -e "DB_PASSWORD=password" \
      update \
      run
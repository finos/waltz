# Waltz Integration Tests

## About

Waltz uses [Zonky Embedded Database](https://github.com/zonkyio/embedded-database-spring-test)
to drive integration testing for both Postgres and MSSQL targets. Zonky directs the
use of [TestContainers](https://github.com/testcontainers/testcontainers-java) or
embedded databases based on the selected provider. When Docker is selected (or
available in `auto` mode), the tests start database containers, run the Liquibase
migrations, and then spin up a cut down Waltz system configured to use that database.
When Docker is not selected or unavailable, the tests fall back to embedded Postgres
for Postgres or H2 in MSSQL compatibility mode for MSSQL.


## Dependencies

To use this testing infrastructure with Docker you must meet the following
TestContainers requirements:

- Docker must be installed, see [supported environments](https://www.testcontainers.org/supported_docker_environment/)
- Access to [DockerHub](https://hub.docker.com/) to download images
  - Note: an account on DockerHub is _not_ required

Embedded Postgres mode additionally requires these dependencies (only when using
`db.provider=embedded` with Postgres):

- `io.zonky.test:embedded-postgres`
- `io.zonky.test.postgres:embedded-postgres-binaries-bom`
  
These two requirements may not be easily met within corporate environments.  As a
result, this testing sub-module is not included by default in the main Waltz build.
It can be enabled by selecting the `integration-tests` profile when executing
Maven tasks, and embedded mode can be used when Docker is not available.

## Configuration

The integration tests use two system properties to select the database and provider:

- `-Dtarget.db=postgres|mssql`
- `-Ddb.provider=docker|embedded|auto`

### Defaults (when properties are not supplied)

- `target.db` defaults to `mssql`
- `db.provider` defaults to `embedded`

With defaults, the tests will target MSSQL and, if Docker is not requested, they will
fall back to H2 in MSSQL compatibility mode (not a real SQL Server instance).

### Provider behavior

- `docker`: always use TestContainers
- `embedded`: for Postgres use embedded Postgres; for MSSQL fall back to H2 in MSSQL
  compatibility mode
- `auto`: use Docker if available, otherwise fall back to embedded Postgres/H2 behavior
 
 
## Running the tests

To run the tests ensure the `integration-tests` Maven profile is selected and either run
the tests from the Maven command line or re-import your Maven config into your
IDE and run the tests from there.
 
Running the tests uses the standard way for executing unit tests.  The tests _will_
take longer than typical tests, especially on the first run when Docker images
are downloaded.

### Examples (repo root)

Postgres via Docker:

```
mvn clean package -P waltz-postgres,dev-postgres,integration-tests -Dtarget.db=postgres -Ddb.provider=docker
```

MSSQL via Docker:

```
mvn clean package -P waltz-mssql-alt,dev-mssql,integration-tests -Dtarget.db=mssql -Ddb.provider=docker
```

Postgres embedded:

```
mvn clean package -P waltz-postgres,dev-postgres,integration-tests -Dtarget.db=postgres -Ddb.provider=embedded
```

### CI dual-build context

See `.github/workflows/README.md` for details on the Postgres + MSSQL dual-build
workflow used in GitHub Actions.

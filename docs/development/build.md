# Building

Note: If you would like to build and run Waltz on MacOS with PostgreSQL, use [this guide](build-and-run-on-mac.md).

Waltz is built using [Maven](https://maven.apache.org/).

## Prerequisites

- [Git](https://git-scm.com/)
- [Maven 3](https://maven.apache.org/)
- [JDK 8+](http://www.oracle.com/technetwork/java/javase/overview/index.html) (see note below)
- [Node](https://nodejs.org/en/)
  - [NPM](https://www.npmjs.com/) v6+
- [Sass](http://sass-lang.com/)
- Database - either
  - [Microsoft SQL Server](https://www.microsoft.com/en-gb/sql-server/)
  - [Postgres](https://www.postgresql.org/)
- [Liquibase](http://www.liquibase.org/) (recommended but not essential)
- [_jOOQ Pro_](https://www.jooq.org/download/) (if using Microsoft SQL Server) 


**Note**:
When developing on JDK 9+ please ensure all class imports are explicit.
This is to prevent collisions between `java.lang.Record` and `org.jooq.Record` which can cause compilation errors (see issue: [#6678](https://github.com/finos/waltz/issues/6678))


## Obtaining the code

It is recommended that you clone the repository on GitHub to maintain your own fork and pull in changes as required.

## Preparing the database

For **Postgres** create a new database
```
create database waltz;
```

For **SQL Server** create a new database
```
CREATE DATABASE waltz ;
```

When you run the first build (using `mvn package`) the schema will be generated using the liquibase maven task (ref: `waltz-schema/pom.xml`).  

### Importing sample data

Starting with release 1.14 (alpha) we will make sample data sets available via the release pages.  
See [Dump and restore](database/dump_and_restore.md) for details on how to import these changes.


## Setting up maven profiles

Waltz uses maven profiles to target the build against the correct database.  Generic db vendor settings are located in  
the profiles section of `<REPO>/pom.xml` and should not need to be changed.

Specific database connection details should be configured in the 
`~/.m2/settings.xml` file.  An example (for both PostreSQL and Microsoft 
SQL Server) would look like:

```xml
<settings>
    <profiles>
        <!--
        these profile are used to provide db connection details
        so that the jOOQ code generator can run.
        -->

        <profile>
            <id>dev-postgres</id>
            <properties>
                <database.url>jdbc:postgresql://localhost:5432/waltz</database.url>
                <database.user>dbuser</database.user>
                <database.password>dbpassword</database.password>
                <database.schema>public</database.schema>
            </properties>
        </profile>
        <profile>
            <id>dev-mssql</id>
            <properties>
                <database.url>jdbc:sqlserver://dbhost:1433;databaseName=waltzdb</database.url>
                <database.user>dbuser</database.user>
                <database.password>dbpassword</database.password>
                <database.schema>dbschema</database.schema>
                <database.catalog>waltzdb</database.catalog> <!-- only req'd for code gen w/ mssql -->
            </properties>
        </profile>
    </profiles>
</settings>
```


## Running the build

Typically one of two maven targets is executed.  For the first run (and whenever schema updates are required) then a full `package` build should be executed.  For code only change then the quicker `compile` target can be used.

When running either variant you must provide the names of two profiles, firstly the generic database profile (either `waltz-postgres` or `waltz-mssql`) and the specific profile created in your `~/.m2/settings.xml` file (in the example above either `dev-postgres` or `dev-mssql`).

Note: `waltz-mssql-alt` uses the newer Microsoft SQL Server driver and is intended to
be the default going forward. The `waltz-mssql` profile remains for legacy users but
is expected to change in a future release.

### Examples (using aliases)

Below are some example maven command lines.  We typically register the command as an alias to save time.

```
alias compile-postgres='mvn clean compile -P waltz-postgres,dev-postgres'
alias compile-mssql='mvn clean compile -P waltz-mssql-alt,dev-mssql'
alias pkg-postgres='mvn clean package -P waltz-postgres,dev-postgres'
alias pkg-mssql='mvn clean package -P waltz-mssql-alt,dev-mssql'
```

## Integration tests

Integration tests run in the `waltz-integration-test` module and are enabled via the
`integration-tests` Maven profile. The tests support Postgres and MSSQL targets and
can run using Docker (TestContainers) or embedded/fallback modes.

Two system properties control the target database and provider:

- `-Dtarget.db=postgres|mssql`
- `-Ddb.provider=docker|embedded|auto`

Defaults (when properties are not supplied):

- `target.db` defaults to `mssql`
- `db.provider` defaults to `embedded`

When `target.db=mssql` and `db.provider=embedded`, the integration tests use H2 in
MSSQL compatibility mode (they do not start a real SQL Server instance).

Example commands (from repo root):

```
mvn clean package -P waltz-postgres,dev-postgres,integration-tests -Dtarget.db=postgres -Ddb.provider=docker
mvn clean package -P waltz-mssql-alt,dev-mssql,integration-tests -Dtarget.db=mssql -Ddb.provider=docker
mvn clean package -P waltz-postgres,dev-postgres,integration-tests -Dtarget.db=postgres -Ddb.provider=embedded
```


## Versioning

```
mvn versions:set -DnewVersion=1.1.10
mvn versions:commit
```

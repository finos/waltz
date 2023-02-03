# Building

Note: If you would like to build and run Waltz on MacOS with PostgreSQL, use [this guide](build-and-run-on-mac.md).

Waltz is built using [Maven](https://maven.apache.org/).

## Prerequisites

- [Git](https://git-scm.com/)
- [Maven 3](https://maven.apache.org/)
- [JDK 8](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Node](https://nodejs.org/en/)
  - [NPM](https://www.npmjs.com/) v6+
- [Sass](http://sass-lang.com/)
- Database - either
  - [Microsoft SQL Server](https://www.microsoft.com/en-gb/sql-server/)
  - [Postgres](https://www.postgresql.org/)
- [Liquibase](http://www.liquibase.org/) (recommended but not essential)
- [_jOOQ Pro_](https://www.jooq.org/download/) (if using Microsoft SQL Server) 

## Obtaining the code

It is recommended that you clone the repository on GitHub to maintain your own fork and pull in changes as required.

## Preparing the database

For **Postgres** create a new daabase
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
the profiles section of `<REPO>/waltz-schema/pom.xml` and should not need to be changed.

Specific database connection details should be configured in the 
`~/.m2/settings.xml` file.  An example (for both MariaDB and Microsoft 
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

When running either variant you must provide the names of two profiles, firstly the generic database profile (either `waltz-mariadb` or `waltz-mssql`) and the specific profile created in your `~.m2/settings.xml` file (in the example above either `dev-maria` or `dev-mssql`).

### Examples (using aliases)

Below are some example maven command lines.  We typically register the command as an alias to save time.

```
alias compile-postgres='mvn clean compile -P waltz-postgres,dev-postgres'
alias compile-mssql='mvn clean compile -P waltz-mssql,dev-mssql'
alias pkg-postgres='mvn clean package -P waltz-postrgres,dev-postgres'
alias pkg-mssql='mvn clean package -P waltz-mssql,dev-mssql'
```


## Versioning

```
mvn versions:set -DnewVersion=1.1.10
mvn versions:commit
```

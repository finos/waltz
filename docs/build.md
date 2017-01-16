# Building

Waltz is built using maven.

## Prerequisites

- Git
- Maven 3
- JDK 8
- Liquibase
- Node
- Sass
- Database - either
  - MariaDB
  - Microsoft SQL Server
- _jOOQ Pro_ (if using Microsoft SQL Server) 

## Obtaining the code

It is recommended that you clone the repository on GitHub (see ...)

## Preparing the database


## Setting up maven profiles

Generic database profiles are located in  
then `<REPO>/waltz-schema/pom.xml` file and should not
need to be changed.

Specific database connection details should be created your accounts
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
            <id>dev-maria</id>
            <properties>
                <database.url>jdbc:mysql://dbhost:3306/waltzdb</database.url>
                <database.user>dbuser</database.user>
                <database.password>dbpassword</database.password>
                <database.schema>waltz</database.schema>
            </properties>
        </profile>
        <profile>
            <id>dev-mssql</id>
            <properties>
                <database.url>jdbc:sqlserver://dbhost:1433;databaseName=waltzdb</database.url>
                <database.user>dbuser</database.user>
                <database.password>dbpassword</database.password>
                <database.schema>dbschema</database.schema>
            </properties>
        </profile>
    </profiles>
</settings>
```


### MSSQL settings

### MariaDB settings

## Running the build

Quick build



### Aliases

These may be useful:

```
alias compile-maria='mvn clean compile -P waltz-mariadb,dev-maria'
alias compile-mssql='mvn clean compile -P waltz-mssql,dev-mssql'
alias pkg-maria='mvn clean package -P waltz-mariadb,dev-maria'
alias pkg-mssql='mvn clean package -P waltz-mssql,dev-mssql'
```


## Versioning

```
mvn versions:set -DnewVersion=1.1.10
mvn versions:commit
```
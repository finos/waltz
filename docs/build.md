# Building

Waltz is built using [Maven](https://maven.apache.org/).

## Prerequisites

- [Git](https://git-scm.com/)
- [Maven 3](https://maven.apache.org/)
- [JDK 8](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Node](https://nodejs.org/en/)
- [Sass](http://sass-lang.com/)
- Database - either
  - [MariaDB](https://mariadb.org/)
  - [Microsoft SQL Server](https://www.microsoft.com/en-gb/sql-server/)
- [Liquibase](http://www.liquibase.org/) (recommended but not essential)
- [_jOOQ Pro_](https://www.jooq.org/download/) (if using Microsoft SQL Server) 

## Obtaining the code

It is recommended that you clone the repository on GitHub to maintain your own fork and pull in changes as required.

## Preparing the database

For Mariadb create a new database:
```
mysql -u root -e "create database IF NOT EXISTS waltz CHARACTER SET='utf8';"
```

When you run the first build (using `mvn package`) the schema will be generated using the liquibase maven task (ref: `waltz-schema/pom.xml`).  

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
                <database.catalog>watlzdb</database.catalog> <!-- only req'd for code gen w/ mssql -->
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

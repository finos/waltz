# Liquibase

Waltz uses [Liquibase](http://www.liquibase.org/index.html) to manage it's schema.
  
The changelog file follows the [best practice guidelines](http://www.liquibase.org/bestpractices.html) outlined
on the liquibase site.


## Change Ids

Id's have undergone several changes since project inception.  The current format is:

`<yyyymmdd>-<issueId>-<counter>`

For example:

`20160302-102-2` can easily be read as the second change relating to issues 102 and was created on 2nd March 2016.
Strictly speaking the date is not required but it helps when searching for changes in a certain time range.


## Executing the changes:

### Sample .bat file (MariaDB)
```
C:/tools/liquibase-3.5.5-bin/liquibase.bat ^
--driver=org.mariadb.jdbc.Driver ^
--classpath=<path to driver>/mariadb-java-client-1.4.6.jar ^
--changeLogFile=<path to changelog master>/db.changelog-master.xml ^
--url="jdbc:sqlserver://<host>:<port>;databaseName=<database>" ^
--username=<user> ^
--password=<password> ^
update
```

### Sample .bat file (MSSQL)
```
C:/tools/liquibase-3.5.5-bin/liquibase.bat ^
--driver=com.microsoft.sqlserver.jdbc.SQLServerDriver ^
--classpath=<path to driver>/sqljdbc4-4.0.2206.100.jar ^
--changeLogFile=<path to changelog master>/db.changelog-master.xml ^
--url="jdbc:sqlserver://<host>:<port>;databaseName=<database>" ^
--username=<user> ^
--password=<password> ^
update
```

### Sample .sh file (MariaDB)
```
#!/bin/sh
~/dev/tools/liquibase/liquibase --driver=org.mariadb.jdbc.Driver \
      --classpath=<path to driver>/mariadb-java-client/1.3.2/mariadb-java-client-1.3.2.jar \
      --changeLogFile=<path to changelog master>/db.changelog-master.xml \
      --url="jdbc:mysql://<hostname>:<port>/<database>" \
      --username=<user> \
      --password=<password> \
      update
```

### Sample .sh file (PostgreSQL)
```
#!/bin/sh
liquibase --driver=org.postgresql.Driver \
      --classpath=<path to driver>/postgresql-42.2.5.jar \
      --changeLogFile=../waltz-data/src/main/ddl/liquibase/db.changelog-master.xml \
      --url="jdbc:postgresql://<host>:<port>/waltz" \
      --username=<user> \
      --password=<password> \
      update
```

Waltz provides sample files:
- `migrate*.*`

Which you may copy and adapt to your environment.


## Generating SQL of the changes:

### Sample .bat file (MariaDB)
```
C:/tools/liquibase-3.5.5-bin/liquibase.bat ^
--driver=org.mariadb.jdbc.Driver ^
--classpath=<path to driver>/mariadb-java-client-1.4.6.jar ^
--changeLogFile=<path to changelog master>/db.changelog-master.xml ^
--url="jdbc:sqlserver://<host>:<port>;databaseName=<database>" ^
--username=<user> ^
--password=<password> ^
updateSQL
```

### Sample .bat file (MSSQL)
```
C:/tools/liquibase-3.5.5-bin/liquibase.bat ^
--driver=com.microsoft.sqlserver.jdbc.SQLServerDriver ^
--classpath=<path to driver>/sqljdbc4-4.0.2206.100.jar ^
--changeLogFile=<path to changelog master>/db.changelog-master.xml ^
--url="jdbc:sqlserver://<host>:<port>;databaseName=<database>" ^
--username=<user> ^
--password=<password> ^
updateSQL
```

### Sample .sh file (MariaDB)
```
#!/bin/sh
~/dev/tools/liquibase/liquibase --driver=org.mariadb.jdbc.Driver \
      --classpath=<path to driver>/mariadb-java-client/1.3.2/mariadb-java-client-1.3.2.jar \
      --changeLogFile=<path to changelog master>/db.changelog-master.xml \
      --url="jdbc:mysql://<hostname>:<port>/<database>" \
      --username=<user> \
      --password=<password> \
      updateSQL
```


### Sample .sh file (PostgreSQL)
```
#!/bin/sh
liquibase --driver=org.postgresql.Driver \
      --classpath=<path to driver>/postgresql-42.2.5.jar \
      --changeLogFile=../waltz-data/src/main/ddl/liquibase/db.changelog-master.xml \
      --url="jdbc:postgresql://<host>:<port>/waltz" \
      --username=<user> \
      --password=<password> \
      updateSQL
```

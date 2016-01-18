#!/bin/sh
~/dev/tools/liquibase/liquibase --driver=org.postgresql.Driver \
      --classpath=/home/dwatkins/.IntelliJIdea15/config/jdbc-drivers/postgresql-9.4-1201.jdbc4.jar \
      --changeLogFile=db.changelog.xml \
      --url="jdbc:postgresql://localhost:5432/waltz" \
      --defaultSchemaName=public \
      --username=dwatkins \
      --password=xxx \
      changelogSyncSQL
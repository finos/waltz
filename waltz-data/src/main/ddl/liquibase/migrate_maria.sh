#!/bin/sh
~/dev/tools/liquibase/liquibase --driver=org.mariadb.jdbc.Driver \
      --classpath=/Users/dwatkins/.m2/repository/org/mariadb/jdbc/mariadb-java-client/1.3.2/mariadb-java-client-1.3.2.jar \
      --changeLogFile=db.changelog-master.xml \
      --url="jdbc:mysql://localhost:3306/scratch" \
      --username=root \
      --password=password \
      migrate

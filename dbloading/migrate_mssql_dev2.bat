C:/Users/woodjes/tools/liquibase-3.7.0-bin/liquibase.bat ^
--driver=com.microsoft.sqlserver.jdbc.SQLServerDriver ^
--classpath=C:/Users/woodjes/.m2/repository/com/microsoft/sqlserver/sqljdbc4/4.0.2206.100_enu/sqljdbc4-4.0.2206.100_enu.jar ^
--changeLogFile=../waltz-data/src/main/ddl/liquibase/db.changelog-master.xml ^
--url="jdbc:sqlserver://LONCGDBU0021:1433;databaseName=WALTZ_DEV" ^
--username=WALTZ_USER ^
--password=WaltzU@123 ^
migrate

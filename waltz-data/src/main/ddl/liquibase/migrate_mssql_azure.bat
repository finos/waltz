C:/tools/liquibase-3.5.1-bin/liquibase.bat ^
--driver=com.microsoft.sqlserver.jdbc.SQLServerDriver ^
--classpath=C:\Users/Kamran\.IntelliJIdea2016.1/config/jdbc-drivers/sqljdbc4-4.0.2206.100.jar ^
--changeLogFile=db.changelog-master.xml ^
--url="jdbc:sqlserver://waltz.database.windows.net:1433;database=waltz" ^
--username=waltz ^
--password=password@123 ^
migrate

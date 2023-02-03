# Setting up Waltz development environment on MacBook with PostgreSQL
Welcome! This guide is to walk you through setting up your development environment on MacOS.

This document describes how to:
1. install Homebrew package manager
1. install git
1. install Java Development Kit 8 (OpenJDK)
1. install maven
1. install Node.js
1. install PostgreSQL and create the Waltz database
1. configure maven profile for PostgreSQL database connection
1. build Waltz
1. run Waltz

This guide is made on 3rd February, 2023 with all versions of the components being latest at this point in time. As you may use this guide later in time, the versions can be different, but the following order and logic will be still applicable.

# Step 1. Install Homebrew
To install required dependencies on MacOS, use the Homebrew package manager. To check wether Homebrew is installed on your local machine, run the `brew --version` command in your Terminal
msagi@miklos-mbp ~ % brew --version
```bash
Homebrew 3.6.20
Homebrew/homebrew-core (git revision c7c38c40e91; last commit 2023-02-02)
Homebrew/homebrew-cask (git revision cc05495e4c; last commit 2023-02-03)
```

If you don't have Homebrew installed, then go to https://brew.sh/ and follow the installation steps.

# Step 2. Install git
MacOS comes with git provided, which can be checked via the `git --version` command
```bash
msagi@miklos-mbp ~ % git --version
git version 2.24.1 (Apple Git-126)
```
if you don't have git installed, then install it via homebrew, using `brew install git` command in your Terminal.

# Step 3. Install JDK 8
To check wether Java Developer Kit version 8 is installed on your local machine, run the `java -version` command in your Terminal.

```bash
It should be Oracle JDK 8, it does not work with OpenJDK@8
msagi@miklos-mbp ~ % java -version
openjdk version "1.8.0_362"
OpenJDK Runtime Environment (build 1.8.0_362-bre_2023_01_22_03_32-b00)
OpenJDK 64-Bit Server VM (build 25.362-b00, mixed mode)
```

If you don't have JDK 8 installed, then install it via homebrew, using `brew install openjdk@8` then `export JAVA_HOME=/usr/local/opt/openjdk@8/libexec/openjdk.jdk/Contents/Home` command in your Terminal.

# Step 4. Install maven
To check wether maven is installed on your local machine, run the `mvn --version` command in your Terminal. It is important that your maven points to JDK 8, otherwise your Waltz build will fail.

```bash
msagi@miklos-mbp waltz % mvn --version
Apache Maven 3.8.7 (b89d5959fcde851dcb1c8946a785a163f14e1e29)
Maven home: /usr/local/Cellar/maven/3.8.7/libexec
Java version: 1.8.0_362, vendor: Homebrew, runtime: /usr/local/Cellar/openjdk@8/1.8.0+362/libexec/openjdk.jdk/Contents/Home/jre
Default locale: en_GB, platform encoding: UTF-8
OS name: "mac os x", version: "13.1", arch: "x86_64", family: "mac"
```

If you don't have maven installed, then install it via homebrew, using `brew install maven` command in your Terminal.


# Step 5. Install Node.js
To check wether Node.js is installed on your local machine, run the `node --version` command in your Terminal. It is important that your maven points to JDK 8, otherwise your Waltz build will fail.

```bash
msagi@miklos-mbp waltz % node --version
v19.6.0
```

If you don't have Node.js installed, then install it via homebrew, using `brew install node` command in your Terminal.

# Step 6. Install PostgreSQL

To check wether PostgreSQL is installed on your local machine, run the `psql -V` command in your Terminal.

```bash
msagi@miklos-mbp ~ % psql -V
psql (PostgreSQL) 14.6 (Homebrew)
```

If you don't have PostgreSQL installed, then install it via homebrew, using `brew install postgresql` command in your Terminal. Once PostgreSQL has been installed, create a folder to keep the database related files in then initialise the database with this folder. In this example, we will create a folder called 'waltz-db'. you have to init the database first using `initdb -D ./waltz-db` command in your Terminal.

```bash
# in this example, we use the 'waltz-db' folder to store the PostgreSQL database files in
WALTZ_DB_FOLDER=waltz-db

# create the new folder
msagi@miklos-mbp waltz-fork % mkdir $WALTZ_DB_FOLDER

# initialise the PostgreSQL database in this folder
msagi@miklos-mbp waltz-fork % initdb -D ./$WALTZ_DB_FOLDER 
The files belonging to this database system will be owned by user "msagi".
This user must also own the server process.

The database cluster will be initialized with locale "en_GB.UTF-8".
The default database encoding has accordingly been set to "UTF8".
The default text search configuration will be set to "english".

Data page checksums are disabled.

fixing permissions on existing directory ./waltz-db ... ok
creating subdirectories ... ok
selecting dynamic shared memory implementation ... posix
selecting default max_connections ... 100
selecting default shared_buffers ... 128MB
selecting default time zone ... Europe/London
creating configuration files ... ok
running bootstrap script ... ok
performing post-bootstrap initialization ... ok
syncing data to disk ... ok

initdb: warning: enabling "trust" authentication for local connections
You can change this by editing pg_hba.conf or using the option -A, or
--auth-local and --auth-host, the next time you run initdb.

Success. You can now start the database server using:

    pg_ctl -D ./waltz-db -l logfile start

msagi@miklos-mbp waltz-fork % 
```

Once the database is created, we can start the PostgreSQL engine:

```bash
# start the database engine
msagi@miklos-mbp waltz-fork % pg_ctl -D ./waltz-db -l waltz-db.log start
waiting for server to start.... done
server started

# track log file output (optional)
msagi@miklos-mbp waltz-fork % tail -f ./waltz-db.log 
2023-02-03 11:57:16.772 GMT [2532] LOG:  starting PostgreSQL 14.6 (Homebrew) on x86_64-apple-darwin22.1.0, compiled by Apple clang version 14.0.0 (clang-1400.0.29.202), 64-bit
2023-02-03 11:57:16.774 GMT [2532] LOG:  listening on IPv6 address "::1", port 5432
2023-02-03 11:57:16.774 GMT [2532] LOG:  listening on IPv4 address "127.0.0.1", port 5432
2023-02-03 11:57:16.775 GMT [2532] LOG:  listening on Unix socket "/tmp/.s.PGSQL.5432"
2023-02-03 11:57:16.778 GMT [2533] LOG:  database system was shut down at 2023-02-03 11:51:49 GMT
2023-02-03 11:57:16.781 GMT [2532] LOG:  database system is ready to accept connections
```

Now that the database is up and running, we can connect to the database first then create the Waltz database scheme.

```bash
psql postgres -c "create database waltz"
CREATE DATABASE
```

# Step 7. Configure maven profile for PostgreSQL database connection details

In order for the jOOQ Waltz component to be able to generate Java code from your database and let you build type safe SQL queries through its fluent API, we have to configure a maven profile, providing connection details to the PostreSQL database.

This profile can be added to the maven settings located in your `~/.m2/settings.xml` file. After adding the database connection details, your settings.xml file can look like this

```xml
<settings>
    <profiles>
        <!-- Waltz PostgreSQL database profile for development -->
        <profile>
            <id>dev-postgres</id>
            <properties>
                <database.url>jdbc:postgresql://localhost:5432/waltz</database.url>
                <database.user>msagi</database.user>
                <database.password></database.password>
                <database.schema>public</database.schema>
            </properties>
        </profile>
    </profiles>
</settings>
```

# Step 8. Build Waltz from source code

Now that all dependencies have been installed, the database is set up and configured, we can build Waltz from source code. This should be relatively easy to do so, as first we clone the source code repository then run the build command from the Terminal.

```bash
# clone Waltz source code repository
msagi@miklos-mbp waltz-fork % git clone git@github.com:finos/waltz.git

Cloning into 'waltz'...
remote: Enumerating objects: 165819, done.
remote: Counting objects: 100% (1987/1987), done.
remote: Compressing objects: 100% (703/703), done.
remote: Total 165819 (delta 820), reused 1869 (delta 784), pack-reused 163832
Receiving objects: 100% (165819/165819), 49.41 MiB | 14.32 MiB/s, done.
Resolving deltas: 100% (88392/88392), done.

# change directory to the waltz main folder
msagi@miklos-mbp waltz-fork % cd waltz

# run the build
msagi@miklos-mbp waltz % mvn clean compile -P waltz-postgres,dev-postgres
# this process can take some time and will produce lots of log messages
# so we only show you the last few lines of the build so you can check your build
# was successful
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for waltz 1.47-SNAPSHOT:
[INFO] 
[INFO] waltz .............................................. SUCCESS [  0.801 s]
[INFO] waltz-common ....................................... SUCCESS [  1.288 s]
[INFO] waltz-model ........................................ SUCCESS [  7.843 s]
[INFO] waltz-schema ....................................... SUCCESS [  8.426 s]
[INFO] waltz-data ......................................... SUCCESS [  4.215 s]
[INFO] waltz-service ...................................... SUCCESS [  2.152 s]
[INFO] waltz-web .......................................... SUCCESS [  2.359 s]
[INFO] waltz-test-common .................................. SUCCESS [  0.383 s]
[INFO] waltz-jobs ......................................... SUCCESS [  2.785 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  30.354 s
[INFO] Finished at: 2023-02-03T16:06:08Z
[INFO] ------------------------------------------------------------------------
```
# Step 9. Run Waltz

Now that we have built Waltz from source code, we can attempt to package and run it. This is very similar to building Waltz, and it is only to execute a maven command which packages up the Waltz code, create a configuration file setting up Waltz properties then execute a java command which starts Waltz from the Terminal.

```bash
# change directory to the waltz main folder
msagi@miklos-mbp waltz-fork % cd waltz

# run the packaging
msagi@miklos-mbp waltz % mvn clean package -P waltz-postgres,dev-postgres
# this process can take some time and will produce lots of log messages
# so we only show you the last few lines of the package so you can check your build
# was successful
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for waltz 1.47-SNAPSHOT:
[INFO] 
[INFO] waltz .............................................. SUCCESS [  0.774 s]
[INFO] waltz-common ....................................... SUCCESS [  8.387 s]
[INFO] waltz-model ........................................ SUCCESS [  9.819 s]
[INFO] waltz-schema ....................................... SUCCESS [ 10.296 s]
[INFO] waltz-data ......................................... SUCCESS [  5.079 s]
[INFO] waltz-service ...................................... SUCCESS [  3.022 s]
[INFO] waltz-web .......................................... SUCCESS [02:24 min]
[INFO] waltz-test-common .................................. SUCCESS [  0.541 s]
[INFO] waltz-jobs ......................................... SUCCESS [  8.253 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  03:10 min
[INFO] Finished at: 2023-02-03T16:22:47Z
[INFO] ------------------------------------------------------------------------
```

Waltz loads it's configuration from the Java classpaths and if no configuration found there then it falls back to the user's `~/.waltz/waltz.properties` file. The Waltz configuration properties file can have several parameters to set based on your custom needs. The following is an example configuration file.
```
database.url=jdbc:postgresql://localhost:5432/waltz
database.user=msagi
database.password=
database.schema=waltz
database.driver=org.postgresql.Driver
jooq.dialect=POSTGRES

database.pool.max=16

waltz.from.email=miklos.sagi@gmail.com
waltz.base.url=http://localhost:8000/
```

We are now ready to start Waltz from the Terminal:

```bash
# change directory to the waltz main folder
msagi@miklos-mbp waltz-fork % cd waltz

# start Waltz (with your configuration file being in ~/.waltz/waltz.properties)
msagi@miklos-mbp waltz % $JAVA_HOME/bin/java -cp ./waltz-web/target/waltz-web-jar-with-dependencies.jar org.finos.waltz.web.Main 


    :::       :::     :::     :::    ::::::::::: ::::::::: 
    :+:       :+:   :+: :+:   :+:        :+:          :+:  
    +:+       +:+  +:+   +:+  +:+        +:+         +:+   
    +#+  +:+  +#+ +#++:++#++: +#+        +#+        +#+    
    +#+ +#+#+ +#+ +#+     +#+ +#+        +#+       +#+     
     #+#+# #+#+#  #+#     #+# #+#        #+#      #+#      
      ###   ###   ###     ### ########## ###     #########

 
--WALTZ---------------------------------------------
Home is: /Users/msagi
Listening on port: 8443
SSL Enabled: false
----------------------------------------------------
```

If you get a error saying Java not found, then revisit the step where we installed JDK 8 and set the JAVA_HOME variable. Perhaps you have to reset this JAVA_HOME variable again.
# Congratulations!
Now you can start exploring Waltz by opening [http://localhost:8443/](http://localhost:8443/)!
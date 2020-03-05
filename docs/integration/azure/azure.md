# Azure Deployment


<!-- toc -->

<!-- tocstop -->

## Status: DEVELOPMENT

This guide is currently being written.  It will almost contain inaccuracies.  PR's welcome.


## Overview

This guide will walk users through a basic setup of Waltz on the Microsoft Azure 
cloud platform.


## Prerequisites

I will assume:

- you have an active Azure account (trial subscription is fine)
- you have a basic understanding of Java/Maven/Tomcat


## Installation

We need:
- Resource Group (e.g. `waltz-khartec`) 
- Tomcat Container (e.g. `waltz-tc`)
- SQL Server (e.g. `waltz-khartec-db-server`) - [image](images/2_azure_add_database_resource.png)
    - with an elastic pool - [image](images/3_azure_add_database_elastic_pool.png)
    - SQL Database (e.g. `waltzdb`)

You should end up with a configuration similar to this [image](images/4_azure_view_group.png)

## Database Configuration
     
For remote connection to database, you will need to change the firewall settings on 
the server and allow connection from your device (`Add Client IP`) - [image](images/5_add_hole_in_sql_firewall_for_laptop.png)  
 
You will need the jdbc connection url for configuring Waltz (via `waltz.properties`). 
This may be obtained from the SQL Server options menu, (`Settings > Connection Strings`) - [image](images/6_get_jdbc_url.png)


### Setting up the schema

If you wish to setup the schema you will need to run liquibase as described in the 
document: [Liquibase setup](https://github.com/finos/waltz/blob/master/waltz-data/src/main/ddl/liquibase/README.md)

### Data

Test data can be generated via a Java application in `waltz-jobs/src/main/..../LoadAll` or 
imported from an example set (pre-built sample sets are available on the Github releases pages).


## Web App deployment / configuration
 
Setup FTP/SFTP using the details given on the app page (`FTP/deployment username`)
and a password from the `deployment center/ftp/user credentials` page.

Copy the war file,`waltz-web.war` (which is either built from source or obtained from the Github 
release page), into the `<ftpsite>/site/wwwroot/bin/apache-tomcat-8.5.24/webapps`.

### Preparing the configuration (waltz.properties)

We need a standard `waltz.properties` file to deploy alongside the Waltz WAR file. An example
is shown below:


```
# Remote Azure
database.url=jdbc:sqlserver://waltz-khartec-db-server.database.windows.net:1433;database=waltzdb;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
database.user=waltz-admin
database.password=xxxx
database.schema=dbo
database.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
jooq.dialect=SQLSERVER2014

database.pool.max=16

waltz.from.email=dwatkins@khartec.com
waltz.base.url=http://localhost:8000/

database.performance.query.slow.threshold=10
```

### Deploying the configuration properties
    
*This section needs to be improved* as there should be a better way to supply the 
runtime properties to the deployed Waltz instance. 

Wait for it to expand then copy a `waltz.properties` file into 
the expanded war at location:
    
    <ftpsite>/site/wwwroot/bin/apache-tomcat-8.5.24/webapps/waltz-web/WEB-INF/classes

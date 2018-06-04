

# Server

## Pre Requisites

* Java 8
* Maven 3
* Tomcat 8 (optional)

## Building

To build the web server use:

    $> mvn clean package  [-DskipTests]

# Configuration

Currently looks on classpath for `waltz.properties` or falls
back to `<home>/.waltz/waltz.properties` for options, should look something like this:

    # database connection detail
    database.url=jdbc:postgresql://localhost:5432/waltz
    database.user=<user>
    database.password=<password>

    # minimum number of connections to the database
    database.pool.min=2
    # maximum number of connections to the database
    database.pool.max=10

A fuller example is [here](https://github.com/khartec/waltz/blob/master/waltz-web/src/main/resources/waltz.sample.properties)

# Running

## Without container (using uber jar)

Launch the server with

    $> cd waltz-web/target
    $> java -cp uber-waltz-web-1.0-SNAPSHOT.jar Main


## With container (i.e. Tomcat)

Deploy the war file in:

    waltz-web/target/waltz-web.war

Ensure `waltz.properties` and and overridden `logback.xml` file
are available (typically on the classpath).

## Both

When the server starts you will see messages about registering
enpoints and CORS services, similar to:

````
....
7:59:43.633 [main] INFO  com.khartec.waltz.web.Main - Registering Endpoint: userEndpoint
17:59:43.637 [main] INFO  com.khartec.waltz.web.Main - Registering Endpoint: authenticationEndpoint
17:59:43.639 [main] INFO  com.khartec.waltz.web.Main - Registering Endpoint: dataExtractEndpoint
17:59:43.643 [main] DEBUG c.k.w.w.e.a.StaticResourcesEndpoint - Registering static resources
17:59:43.644 [main] INFO  com.khartec.waltz.web.Main - Completed endpoint registration
17:59:43.649 [main] INFO  com.khartec.waltz.web.Main - Enabled CORS
````




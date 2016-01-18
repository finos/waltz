

# Server

## Pre Requisites

* Java 8
* Maven 3

## Building

To build the web server use:

    $> mvn clean package  [-DskipTests]

## Configuration

Currently looks in `<home>/.waltz/waltz.properties` for options, should look something like this:

    # database connection detail
    database.url=jdbc:postgresql://localhost:5432/waltz
    database.user=<user>
    database.password=<password>

    # minimum number of connections to the database
    database.pool.min=2
    # maximum number of connections to the database
    database.pool.max=10


## Running
Launch the server with

    $> cd waltz-web/target
    $> java -cp uber-waltz-web-1.0-SNAPSHOT.jar Main


You should see messages about registering endpoints

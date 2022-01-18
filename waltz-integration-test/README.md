# Waltz Integration Tests

## Note: Removed TestContainers temporarily

As of 1.36 we have removed integration testing with TestContainers (using docker images).  This is due to requirements to run behind a corporate firewall with no dockerhub access.  We will restore Testcontainers when we determine how best to proceed.


## About

Waltz uses [TestContainers](https://github.com/testcontainers/testcontainers-java)
project to provide integration testing support.  TestContainers allows JUnit tests
in Java to spin up a docker database container (Postgres), run the Liquibase migration
code against it and then spin up a cut down Waltz system which is configured to 
use that database.


## Dependencies

To use this testing infrastructure you must meet the following TestContainers 
requirements:

- Docker must be installed, see [supported environments](https://www.testcontainers.org/supported_docker_environment/)
- Access to [DockerHub](https://hub.docker.com/) to download images
  - Note: an account on DockerHub is _not_ required
  
These two requirements may not be easily met within corporate environments, hence this
 testing sub-module is not included by default in the main Waltz build.  It can be 
 enabled by selecting the `ci` profile when executing Maven tasks.  See the 
 `profiles > profile > ci` section within the `waltz\pom.xml` file to see how this is
 configured.
 
 
 ## Running the tests
 
 To run the tests ensure the `ci` maven profile is selected and either run
 the tests from the maven command line or re-import your maven config into your 
 IDE and run the tests from their.
 
 Running the tests uses the standard way for executing unit tests.  The tests _will_
 take longer than typical tests, especially on the first run when Docker images
 are downloaded.
  
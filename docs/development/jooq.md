# jOOQ

Waltz makes use of the excellent [jOOQ](http://www.jooq.org/) library as it's data access layer.

jOOQ is dual licensed and may be used free of charge if you are using Maria or Postgres as your Waltz database.
If you are using SQLServer as your database a commercial [Professional License](http://www.jooq.org/legal/licensing) 
_must_ be obtained.  

## Upgrading

As we cannot include the commercial variant of the jOOQ libraries you must download and install the library using the 
scripts provided in the downloaded zip.   If you are using Maria/Postgres then the Maven will automatically download 
the latest from Maven Central.

Within Waltz, jOOQ is configured in the `waltz-schema/pom.xml` config file.  In particular the version
is specified via the property: `jooq.version`
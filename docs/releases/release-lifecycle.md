# Release Lifecycle

This document outlines the key activities to be performed on each minor (x.y) release of Waltz.  

## Start

At the start of a new release, when development commences the following should be performed (an issue for each should be created).

### Java

#### Java Library Updates

Check for major version upgrades for any of the libraries used by Waltz.  This can be achieved via the following command:

`mvn versions:display-dependency-updates`

You'll probably want to direct the output of this command to a file for analysis.

_Further Reading_  

- [Versions plugin](http://www.mojohaus.org/versions-maven-plugin/)


#### Java Library Vulnerability Scan

The dependency check plugin can be used to report on libraries which have a vulnerability recorded against them.  It contributes to the maven verify phase:

`mvn verify -Pwaltz-maria,mac-maria`

(Substitute whatever profiles you use for building) 
This can take several minutes and files are written to `*/target/dependency-check-report.html`

_Further Reading_

- [Dependency Check Plugin](https://www.owasp.org/index.php/OWASP_Dependency_Check)
- [Detect Security Holes during Build](https://blog.hackeriet.no/detect-security-holes-during-build/)


### Node

##### Library Updates

We use `npm-check` to generate a report of javascript library updates

_Further Reading_

- [npm-check](https://github.com/dylang/npm-check)


### Database

#### Liquibase file creation

Each Waltz release should have all of it's schema changes recorded in liquibase.  Each release should have it's own liquibase file located in `waltz-data/src/main/ddl/liquibase` and called `db.changelog-x.y.xml` (where `x.y` is the version number).  This filename should then be recorded in the `db.changelog-master.xml` file.

#### Table decomm

If a table is no longer needed it should be deleted two releases after it has been determined that it is no longer needed.  A tracking issue should have been created.  These tasks should be completed early in the release lifecycle to catch any unanticipated knock-on issues.


# Waltz

In a nutshell Waltz allows you to visualize and define your organisation's technology landscape. Think of it like a structured Wiki for your architecture.

[Feature list](docs/features/README.md)

[Learn more](http://www.waltz-technology.com/)

[Building notes](https://github.com/khartec/waltz/blob/master/docs/build.md).

[Running](https://github.com/khartec/waltz/blob/master/waltz-web/README.md)

---

[![Build Status](https://travis-ci.org/khartec/waltz.svg?branch=master)](https://travis-ci.org/khartec/waltz)

[![Language Grade: JavaScript](https://img.shields.io/lgtm/grade/javascript/g/khartec/waltz.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/khartec/waltz/context:javascript)

[![Language Grade: Java](https://img.shields.io/lgtm/grade/java/g/khartec/waltz.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/khartec/waltz/context:java)


## Corporate Contributors

![DB Logo](https://www.gps-data-team.com/poi/icons/DB-DE.bmp "Deutsche Bank") Deutsche Bank - [press release](https://www.db.com/newsroom_news/2018/deutsche-bank-takes-next-step-in-open-source-journey-en-11484.htm) 

## Technology Stack

### Server

- Java 8
- Embedded Jetty or WAR file (tested on Tomcat 7/8)
- Spark framework
- JDBC via JOOQ


### Supported Databases

- MariaDB
- Microsoft SQL Server (2012+)  
  - requires [JOOQ Pro license](https://www.jooq.org/download/) to build from source


### Client

- Browser based
    - IE 10+, Chrome, Safari, Firefox)
- AngularJS 1.5
- Bootstrap 3
- D3 

(see [package.json](https://github.com/khartec/waltz/blob/master/waltz-ng/package.json) for full list)

## Contact

- email: [mailto:dwatkins@khartec.com]


## Contributors

All contributions should contain the standard license and copyright notice (see [Notice file](NOTICE.md)).  

Individual and organisational contributors are listed in [the contributors file](CONTRIBUTORS.md)

## External Resources

[Using ArchUnit to formalize architecture rules in the Waltz codebase](https://medium.com/@davidwatkins73/using-archunit-to-formalize-architecture-rules-in-the-waltz-code-base-5fd3e092fc22)

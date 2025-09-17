# Waltz

In a nutshell Waltz allows you to visualize and define your organisation's technology landscape. Think of it like a structured Wiki for your architecture.


Learn more
  - [Features](docs/features/README.md)
  - [Product Site](https://waltz.finos.org/)
  - [Blog](https://waltz.finos.org/blog/)
  - [FINOS Announcement](https://www.finos.org/blog/introduction-to-finos-waltz) 
    - now part of the [Linux Foundation](https://www.linuxfoundation.org/blog/2020/04/finos-joins-the-linux-foundation/)

Getting started
 - [Building](docs/development/build.md) 
 - [Running](waltz-web/README.md)
 - [Docker](docker/README.md)

---
[![FINOS Hosted Platform - Waltz Demo](https://img.shields.io/badge/FINOS%20Hosted%20Platform-Waltz%20Demo-blue)](https://demo.waltz.finos.org/)
[![postgres build](https://github.com/finos/waltz/actions/workflows/maven.yml/badge.svg)](https://github.com/finos/waltz/actions)
[![Latest Version](https://badgers.space/github/release/finos/waltz)](https://github.com/finos/waltz/releases)
[![FINOS - Active](https://cdn.jsdelivr.net/gh/finos/contrib-toolbox@master/images/badge-active.svg)](https://finosfoundation.atlassian.net/wiki/display/FINOS/Active)

## Corporate Contributors

| Org                                                                                       |                 | Notes                                                                                                                    |
|-------------------------------------------------------------------------------------------|-----------------|--------------------------------------------------------------------------------------------------------------------------|
| ![DB Logo](https://avatars1.githubusercontent.com/u/34654027?s=30&v=4 "Deutsche Bank")    | Deutsche Bank   | [press release](https://www.db.com/news/detail/20180207-deutsche-bank-takes-next-step-in-open-source-journey) |
| ![NWM Logo](https://avatars2.githubusercontent.com/u/54027700?s=30&v=4 "NatWest Markets") | NatWest Markets | [press release](https://www.nwm.com/about-us/media/articles/natwest-markets-to-expand-open-source-coding)                |
| ![HMx Logo](https://avatars.githubusercontent.com/u/29101549?s=30&v=4 "HMx Labs")        | HMx Labs        | [press release](https://cloudhpc.news/hmx-labs-waltz/)                |

## Technology Stack

### Server

- Java 8
- Embedded Jetty or WAR file (tested on Tomcat 7/8)
- Spark framework
- JDBC via JOOQ

See [pom.xml](https://github.com/finos/waltz/blob/master/pom.xml) for a full list of Java dependencies.


### Supported Databases

- Postgres
- Microsoft SQL Server (2017+)
    - requires [JOOQ Pro license](https://www.jooq.org/download/) to build from source


### Client

- Browser based
    - Chrome, Safari, Firefox, Edge
- AngularJS 1.7
- Svelte
- Bootstrap 3
- D3

See [package.json](https://github.com/finos/waltz/blob/master/waltz-ng/package.json) for full list of javascript
dependencies.

## Roadmap / Releases

Checkout [the project milestones](https://github.com/finos/waltz/milestones) and browse through the Todo, work in
progress and done issues.

**Provisional** dates for upcoming releases:

| Release | Date         | Provisional Change Summary                 |
|---------|--------------|--------------------------------------------|
| 1.71    | 06 June 2025 | Data Type support in Assessment Definition | 
| 1.72    | July 2025    | Provisional                                |

See the [releases](https://github.com/finos/waltz/releases) page for historic versions.



## Contributing

1. Fork it (<https://github.com/finos/waltz/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Read our [contribution guidelines](CONTRIBUTING.md)
   and [Community Code of Conduct](https://www.finos.org/code-of-conduct)
4. Commit your changes (`git commit -am 'Add some fooBar'`)
5. Push to the branch (`git push origin feature/fooBar`)
6. Create a new Pull Request

_NOTE:_ Commits and pull requests to FINOS repositories will only be accepted from those contributors with an active, executed Individual Contributor License Agreement (ICLA) with FINOS OR who are covered under an existing and active Corporate Contribution License Agreement (CCLA) executed with FINOS. Commits from individuals not covered under an ICLA or CCLA will be flagged and blocked by the FINOS Clabot tool. Please note that some CCLAs require individuals/employees to be explicitly named on the CCLA.

*Need an ICLA? Unsure if you are covered under an existing CCLA? Email [help@finos.org](mailto:help@finos.org)*

## Contributors

All contributions should contain the standard license and copyright notice (see [Notice file](NOTICE.md)).  

Individual and organisational contributors are listed in [the contributors file](CONTRIBUTORS.md)


## License

Copyright (C) 2024 Waltz open source project

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

SPDX-License-Identifier: [Apache-2.0](https://spdx.org/licenses/Apache-2.0)

## Security Reporting

Please refer to the Waltz [Security Policy](SECURITY.md) page.

# Waltz Features

## History

Waltz has been under continuous development since 2016.
It was contributed to the FINOS foundation in 2020.

---------

## <a name='apps'></a> Application Metadata

One of the primary entities in Waltz is the _Application_.
Applications have some basic properties such _name_, _type_, and _description_. 
Additional properties include _lifecycle_ indicators (status, dates) and _organizational ownership_ information.

### <a name='app-extensions'></a> Extensions

As with other entities in Waltz, applications can be enriched with additional data points:

- _Involvements_ which link _people_ to apps via a formal role (e.g. Architect). 
  - Involvements can be used for fine-grained permissioning.
- _Assessments_ are used to associate arbitrary enumerated data points with an application.
These can be grouped and are either imported from other systems as read-only values or captured and managed directly in Waltz.
Assessments are versatile and have been used for a wide range of purposes including:
  - Cloud migration tracking
  - Legal Holds
  - Information Classification and Criticality
  - SDLC compliance indicators
  - Recovery time objectives
- Categorized _Bookmarks_ allow for a standardized way of collecting a common set of links to additional information resources about the application
- _Taxonomy Mappings_, which are further [discussed below](#taxonomies)
- _Technology_: servers, and databases can be associated to apps. In addition OSS library components can be linked ([discussed below](#oss-usage))  

--------

## <a name='taxonomies'></a> Taxonomies and Taxonomy Mappings

Organizations can create multiple _Taxonomies_ in Waltz.
Each taxonomy is a hierarchical collection of related elements. 
Examples of these taxonomies are:

- Business Functions
- Capabilities
- Processes
- Regulations
- Agile Domains and Tribes
- Records
- Technology products
  - usage of infra such as messaging systems, databases, workflow tools etc.
- Contracts
  - a taxonomy opf contracts linked to products can be transitively applied to applications based on their technology product usage

### <a name='portfolio-management'></a> Portfolio Management

Organizations can describe and document their application portfolio using _mappings_ to these taxonomies.
Each mapping has an associated _rating_, typically a buy-sell-hold indicator, but the _rating scheme_ can be customised.

The mappings may also be flagged as _primary_ for an application, which allows for more contextual information to be presented when lists of apps are shown elsewhere in Waltz.

### <a name='roadmaps'></a> Roadmaps
Decommission and commission information can be associated with each mapping, allowing detailed roadmaps, showing how the portfolio will change over time, to be sourced from the captured data.

### <a name='allocations'></a> Allocations
The mappings may have allocation percentages associated to them, this is commonly used to allow a breakdown of costs per capability for applications (and then aggregated across collections of applications).

### <a name='taxonomy-rating-assessments'></a> Rating Assessments
Finally, we recently added the ability to add assessments to ratings.
This supports a more nuanced view of the applications, for example we could add product and location assessments to capability ratings indicating what types of financial products and in what location are being handled by a specific application capability.

### <a name='app-rationalisation'></a> Application Rationalisation
Once the application portfolio has been mapped against a consistent set of taxonomies it becomes simple to see high-level opportunities for app consolidation/rationalisation by viewing all applications which are associated to a taxonomy item.


--------

## <a name='data'></a> Data Management

### <a name='data-types'></a> Data Types

Waltz supports a hierarchical taxonomy of data types.
Each type may be declared as `concrete` indicating it is sufficiently granular to be used when documenting flows.

### <a name='data-flows'></a> Data Flows

Logical flows (generic) and physical flows (specific descriptions of flows) can be captured and linked to data types.

Each logical flow provides information about a data flow between a source and target system.
These systems are typically applications but may be _Actors_ which are used to represent internal and external entities such as _Exchanges_ and _Risk Officers_.
Taken together, the flows provide a directed graph which shows the raw topology of your organization.

The Waltz UI allows flows to be maintained and has functionality to do more advanced operations such as _merging_ duplicate flows.

Flows and their data types can be attested by either a named set of users or users specifically associated (via a specific role) with the source or target applications.

Physical flows provide more detail about specific data flows.
They can be linked to a specification which provides field level detail with optional linkage to a data dictionary.
Specifications can be shared and are often used to represent standardised data schemas within an organisation.

### <a name='lineage'></a> Lineage

Waltz has rudimentary support for describing lineage using _Flow Diagrams_, these diagrams are created using a _constrained editor_ which only allows registered flows to be used.

Each diagram is stored as a _Bill-of-Materials_ allowing aggregated information to be calculated, _Aggregate views_ are discussed later.
The diagrams allow for overlaying and filtering, for example you could overlay an indicator showing which of the application in the diagram perform _Trade Capture_.

For more detailed and comprehensive support for lineage we commonly use the data in Waltz with systems such as _Solidatus_.
Alternatively, the information can be exported into a graph database for further analysis using custom tooling.

### <a name='data-classifications'></a> Flow Classifications

Rules can be created to capture concepts such as _Authority Statements_.
Each rule defines a source, data type, consumer set, and outcome.
For example: '_BookMgr_ (source) is an _Authoritative Source_ (outcome) of _Book Data_ (data type) for the _Investment Bank_ (consumer set)'.  

Defining these rules allows them to be captured and applied to the data flow model to measure 'right sourcing' within an organisation.

By defining tight (sometimes singular apps) as the consumer set Waltz can allow for concepts such as '_Heritage_' flows - those that are non-authoritative but allowed for legacy reasons. 

### <a name='data-assessments'></a> Data Assessments and other extensions

Flows, data types, and related entities can use the standard Waltz enrichment mechanisms.
Particularly useful are assessments, bookmarks, and links to entities such as people (e.g. _Data Owners_) and groups.

--------

## <a name='surveys'></a> Surveys

Waltz provides a survey feature which is deeply integrated into the Waltz data model.
Surveys can be designed and issued against applications (or change initiatives) matching a set of criteria and assigned to users via their involvement to the application.

For example a survey could be issued to all applications which perform any payment related capability and be assigned to the business analysts and business owners of the relevant apps.

### <a name='survey-lifecycle'></a> Survey Lifecycle

Surveys have a basic lifecycle support with an optional approval step once the survey is completed.
It is common for custom code to perform additional tasks through the survey lifecycle. 
Via custom code survey creation may be triggered via an external event.
It is also common for an assessment to be set upon survey approval, for example a survey pertaining to '_Cloud Onboarding_' may result in a '_Cloud Migration Recommendation_' assessment to be assigned.

### <a name='survey-templates'></a> Survey templates and predicates

The surveys themselves can be quite sophisticated and reference Waltz model elements (taxonomies, people etc).
The questions can be shown/hidden by predicates which are either based on other answers in the survey, or they may be predicated on environmental attributes related to the application (e.g. only show a question if the application has a '_High Criticality Rating_'). 


--------

## <a name='ci'></a> Change Initiatives

Change Initiatives are used to capture change programmes and projects in an organization.
They can be associated with applications, people and other Waltz entities.
It is common to use change initiatives, surveys and assessments in conjunction to perform '_Architectural governance_' over the organizations change programmes.


--------

##  <a name='oss'></a> OSS 

Waltz is used to track consumption of open source in applications across an organisation.

###  <a name='oss-usage'></a> Library Usage tracking - OSS Risk

You can associate applications with their dependencies (down to the version level) we can quickly cross-reference with any of the other Waltz data points to see how libraries are used.  This association is done via external processes, such as custom integration with the [_XRay_](https://jfrog.com/solution-sheet/jfrog-xray/) tool from [_JFrog_](https://jfrog.com/).


###  <a name='oss-licences'></a> OSS Licence tracking - Licence Risk

Waltz also allows for OSS licences to be imported (there is a sample loader and an admin ui to support ad-hoc additions).
OSS licences are linked to the software licences mentioned above and are therefore transitively associated to applications.

Typically several assessments will be attached to licences indicating their suitability for use under specific conditions (internal, public-web, client side installation, etc).

--------

##  <a name='aggregate-views'></a> Aggregate views

The features we have discussed so far are mostly concerned with single entities (apps, change initiatives).
Waltz has comprehensive support for viewing the organizations portfolio by many different _vantage points_ - simply a term we use to represent any element which can be used to group the portfolio.
Vantage points are often hierarchical and Waltz will accordingly show an aggregated view.

### Views

Waltz supports many aggregated views.
Some of the most common and useful views are listed below:

- Taxonomies
  - From a vantage point Waltz can aggregate across all taxonomies.  This can be used to show how apps that are linked ato a _Payments_ capability (the vantage point) may be related to the process taxonomy (the view)
- Flows
  - The applications in a vantage point can be used to produce an aggregate view of the related data flows.  This view can either be _intra_ flows (only flows between nodes in the vantage point), _outbound_ (flows 'leaving' the vantage point), _inbound_ (flows coming into the vantage point), or _all_
  - The flows are scored against the flow classification rules giving a quick and easy way to see how well the given vantage point is aligning to 'right sourcing' rules
- Attestations
  - Waltz supports attestation of data sets (e.g. taxonomy mappings or flows). Vantage points allow users to see what portion of the included applications have a recent or overdue attestations
- Costs
  - For a given vantage point users can quickly see which applications are the most costly according to whatever cost data has been associated with apps (e.g. Support costs, Infra costs)

#### <a name='report-grids'></a> Report grids

Report grids are a very powerful mechanism which allow any user to simply build self-service reports from Waltz.
A large proportion of the Waltz data set can be included into a report grid (e.g. taxonomies, data types, assessments, people, costs, survey responses).
Derived columns can be set up which compute additional data points.

Once defined, a grid can be used across all vantage points and shared with other users or made public to all users.

Report grids have been widely used for both ad-hoc and systematic data analysis, supporting several key processes in organisations.
The contents of a report grid can be exported via APIs and a custom (though currently not open-sourced) wrapper was developed to expose grids via the [OData](https://www.odata.org/) protocol so they could be wired into Tableau reports.

### <a name='vantage-points'></a> Typical vantage points

- By taxonomy
  - Any of the taxonomies can be used and the vantage point is constructed of all applications with any mappings to the taxonomy item (or any of it's children)
- By people
  - Using the person involvements to applications and change initiaitives allows Waltz to show a custom view for that person, which included all of their direct and indirect reports. This allows is especially useful for quick comparisons across divisional leads.
- By org unit
  - A very common aggregate view is to use the organisational units to view details on the apps which belong to various departments
- By data type
  - The data type hierarchy can be used by aggregating apps based on data flows which reference the data types 
- By diagram
  - Diagrams in Waltz are often broken down into a '_Bill-of-Materials (BoM)_'. Waltz can use this BoM as the source for the aggregate views.
- By arbitrary group
  - A very popular feature is to allow users (or automated processes) to create custom groups of apps (and change initiatives).  These groups are non-hierarchical but are very flexible.  We periodically review 'structured' sets of groups to see if there is an underlying taxonomy that needs to be surfaced.


#### Bank on a Page

Waltz has the ability to visualize capability/taxonomy for a specific function (e.g., operations, CIO-specific views) - and overlay various metadata (costs, regions, locations, etc. ) with roadmap/timeline to show volatilities/projections. Dynamically generated vantage points.


--------

## Integration

Waltz ships with **no** _out of the box_ integrations.
Integration is typically done via periodic batch jobs which load data into the Waltz database.

Users have integrated with systems such as [Collibra](https://www.collibra.com/us/en), [Solidatus](https://www.solidatus.com/), [Service Now](https://www.servicenow.com/uk/), [Apptio](https://www.apptio.com/), [Sparx Enterprise Architect](https://sparxsystems.com/products/ea/) and more.
We are hoping to offer standardised integrations in the future, with Collibra (Cloud) being the likely first offering.

Specific examples are: 
- using Collibra for providing the Data classes and authority statements.
- using Sparx for the process taxonomy
- using Service now for CMDB information


### Software Development Lifecycle (SDLC)

_Integration with service now to provide controls around dq and enforce regular attestations_



---------


## NFR

### <a name='nfr-security'></a> Security

SSL for browser/server comms.
SSO or OAuth (community developed) integration for authentication.
Basic role permissions and/or a granular permission framework based on person involvements to entities (e.g. restrict application editing to only the _domain architect_ and _IT owner_).
Database encryption as per jdbc driver support.

### <a name='nfr-standards'></a> Standards
Application uses standard web technology.
Accessibility is a focus for future development and is part of the reason for moving to Svelte where the framework helps to ensure accessibility is addressed.

### <a name='nfr-testing'></a> Testing
A mixture of unit testing (junit and mocha) and integration testing (also junit, with playwright) is used.
Architecture tests (layering etc) are performed using [ArchUnit](https://www.archunit.org/).

Increasing coverage is a focus for upcoming releases.

### <a name='nfr-capacity'></a> Capacity
Waltz is happily serving several hundred users per day, out of a total user base of tens of thousands.
The Waltz instance has information on over 5K applications and a similar number of change initiatives. There are many more dataflows, surveys, and assessments managed by Waltz.
Several of the taxonomies stretch to thousands of nodes with many more related mappings.

This is all served by a load balanced pair of Tomcat servers with on a shared JVM of just 2GB.
The (SQLServer) database has 8GB of memory.


### <a name='nfr-releases'></a> Releases
Waltz typically has a major release, requiring a Liquibase scheme migration, every month or two.  Patch releases are more frequent and produced as required.  All releases are available via the [Waltz GitHub releases page](https://github.com/finos/waltz/releases)

### <a name='nfr-licence'></a> Licence
Waltz is released under the [Apache 2.0 Licence](https://github.com/finos/waltz?tab=Apache-2.0-1-ov-file#readme) and is part of the FINOS foundation.

---------

## <a name='stack'></a> Technical Details

### Frontend

Major libraries / frameworks being used:

- [Svelte](https://svelte.dev/), a lightweight performant framework being used in Waltz to replace the legacy [AngularJS](https://angularjs.org/) code
- [D3](https://d3js.org/) for data visualization
- [Lodash](https://lodash.com/) for data manipulation
- [Slickgrid](https://slickgrid.net/) for large tables, being used to replace [UI-Grid](http://ui-grid.info/) (AngularJS based grid library)
- [Sass](https://sass-lang.com/), CSS pre-processor
- [Webpack](https://webpack.js.org/), build framework
- [Mocha](https://mochajs.org/), unit testing

### Server

- [Java](https://www.java.com/en/), currently supporting 8, but shortly moving to JDK 11 and then 17
- [Spring framework](https://spring.io/projects/spring-framework) for auto-wiring components
- [Spark Java](http://sparkjava.com/), a lightweight framework for creating REST endpoints
- [jOOQ](https://www.jooq.org/), a java DSL for writing and executing database queries
- [Immutables](https://immutables.github.io/) for domain objects

### Database

- Schema management: Waltz uses [Liquibase](https://www.liquibase.com/) to ensure accurate and easy scheme migrations 
- Production databases: [MSSQL](https://www.microsoft.com/en-gb/sql-server/), [Postgres](https://www.postgresql.org/)
  - If MSSQL is chosen, and you wish to actively work on the Waltz code base a commercial jOOQ licence is required.  jOOQ is free for free databases, but not for commercial databases 
- Test databases: [H2](https://www.h2database.com/html/main.html)

### Deployment options

- Web container, e.g. [Tomcat](https://tomcat.apache.org/)
- Standalone JAR
- [Docker](https://www.docker.com/) image


---------

# <a name="plans"></a> Current Development Themes 

## 2024

Currently, the main focus for 2024 is to improve support around data flows and lineage.
This will include extending flow classification rules to include 'authoritative consumers', adding a request based model to flow modifications and registration, and improved support for linking EUDAs to flows.

In addition to flows, we expect to look at tools to boost data quality, specifically by allowing organizations to set up correlation rules between taxonomies.
An evaluator would evaluate the rules and raise potential issues where data does not '_look right_'.
For instance a rule could be set up to say that the '_KYC_' capability expects to see '_Person_' related data and be involved in one or more of a set of processes.

Another theme is around the _Bank on a Page_ feature set.
This may involve closer integration with other reporting tools ([Tableau](https://www.tableau.com/en-gb), [Superset](https://superset.apache.org/)) or we may pursue more custom visualisations.
We also intend to investigate how to integrate more closely with tools such as [Archi](https://www.archimatetool.com/) and other open source diagramming tools ([plantuml](https://plantuml.com/), [d2](https://d2lang.com/), [c4](https://c4model.com/) etc).




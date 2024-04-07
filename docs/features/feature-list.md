Original file from Wells Fargo.  They want an open source view of Waltz features and capabilities


Capability   / Feature | Waltz                                                                                                                                                                                                                                                                                                                                                                            | More                                                            
-- |----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------
Enterprise Architecture   Management | 
Standards | 
Patterns | 
Authoritative Source | Ability to disposition for a   given data type - what is the authoritative source for the data; can   accommodate for heritage scenarios                                                                                                                                                                                                                                         | [Auth sources, including heritage flows](#data-classifications) 
Authoritative Consumers | Ability to identify who should   be the authoritative consumers for a given dataset (bi-directional   relationships for data consumption)                                                                                                                                                                                                                                        
Manage Architectures | x                                                                                                                                                                                                                                                                                                                                                                                
Models | 
Assessments | Assessements can be associated to most entities in Waltz  |                                                                 | 
Portfolio Management | x                                                                                                                                                                                                                                                                                                                                                                                
Application Rationalization | x                                                                                                                                                                                                                                                                                                                                                                                
Roadmaps (application,   products) | x                                                                                                                                                                                                                                                                                                                                                                                
Functional   commissions/de-commissions | Ability to show when   capabilities start/stop within a portfolio                                                                                                                                                                                                                                                                                                                
Lifecycle Mappings | x                                                                                                                                                                                                                                                                                                                                                                                
Data Lineage | Logical flows captured (point to   point with data-type metadata via integration method) with   visualization/mapping abilities.     Significant maturity planned.                                                                                                                                                                                                               
Bank / Architecture on a Page | Ability to visualize   capability/taxonomy for a specific function (e.g., operations, CIO-specific   views) - and overlay various metadata   (costs, regions, locations, etc. ) with roadmap/timeline to show   volatilities/projections. Dynamically generated vantage points.                                                                                                  
Data Products | Data-based reference view of   underlying datamodels for application portfolio for consumption and usage   purposes.                                                                                                                                                                                                                                                             
Cost allocations - Taxonomies | Ability to demarcate the   percentage of costs (shredding) for the purposes of planning/budgeting of a   given function. Application may be performing multiple capabilities and needs   to have appropriate allocations for each functional usage                                                                                                                               
Application Ratings based on   Capability | Application may have a   legal-hold; supports a given capability and assigning a rating. Providing a   more nuanced/detailed view of what an application is doing specifically   within the capability.                                                                                                                                                                          
Supply Chain / Contract   Management | Taxonomy of contracts; associate   application with capability/service and with a contract - providing pivot   views of a vendor-relationship what are all the applications linked to it.                                                                                                                                                                                        
Investment Planning | Built-in to the ratings   function/service/capability. Provide a ratings-item which details the   strategic / attribute alignment. Providing driving alignment views for a   given application.                                                                                                                                                                                  
Analytics / Ad-hoc | x                                                                                                                                                                                                                                                                                                                                                                                
Diagramming | Flow-diagrams based on   underlying metadata (constrained diagram - upstream/downstream graphs). Plans   to link with Archie - leverage reference data with Archie.      Ability to incorporate Archie / Sparx / BPM diagrams into Waltz and   leveraged for overlay purposes (Bank/Architecture on a Page).                                                                     
Analytics / Reporting | ReportGrid   (API-enabled) ; ability to select various data   dimensions /cross-reference (meta-data) and develop derived columns that can   be leveraged to showcase a portfolio's dispositions. Ability to regex for   terms to enhance dispositions based on keyword look-ups. Plans to be leveraged by other product   reporting tools (e.g., Tableau, Apache Superset, etc) 
 | 
 | 
Data | 
Data Model | OLTP model; Flows of data-types;   hierarchical model; leveraging Collibra model; can accommodate for   migrations                                                                                                                                                                                                                                                               
 | 
User Interface | 
American Disability Act | Angular (v1.8); migration to   Svelte; modular sections managed by   Javascript;                                                                                                                                                                                                                                                                                                 
Security   Model (Ability to leverage Channel Secure / AD-ENT   authentication / entitlement services) | SSO enabled; Auth enabled;   open-source contributions from community of users                                                                                                                                                                                                                                                                                                   
Data Sensitivity | Database level encryption;   planned for finer-grained entitlements for sensitive data                                                                                                                                                                                                                                                                                           
System integrations | 
CMDB | ServiceNow (planned   bi-directional) ; Collibra Cloud (planned)                                                                                                                                                                                                                                                                                                                 
iGrafx / BizzDesign / any   other notable vendors | 
API (What standard APIs are   available for the package?) | ServiceNow (planned   bi-directional; integrated into checkpoints to trigger notifications of an   application / architecture sunrise gate) ; Collibra Cloud (planned)                                                                                                                                                                                                           
Stack | Database - Postgresql ; JooQ   (transformation layer/ DSL);     Java (v8+>>11/17); JooQ; immutables library; Spark API DSL >>   javelin >> springboot. Liquidbase                                                                                                                                                                                                                
Open Source / Non-proprietary   technologies | OpenSource since inception   (2016); Apache public license 2                                                                                                                                                                                                                                                                                                                     
Which   components of the system change as a result of any business functionality   changes? | 2 types of releases; major   version release monthly and coincide with database change; minor patch releases as needed                                                                                                                                                                                                                                                           
How are   modifications to the application made (business logic and/or data   structures)? | 
Cost / Licensing |


## <a name='apps'></a> Applications

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
- _Taxonomy Mappings_, which are further discussed below

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

## <a name='data'></a> Data Management Features

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

## Aggregate views

These views are aggregated if higher level items in the taxonomy are selected.

### Vantage points

- By taxonomy
- By people
- By data type
- By org unit
- By diagram
- By arbitrary group

### Views

- Flows
- Attestations
- Taxonomies
- Report grids


#### Bank on a Page

Waltz has the ability to visualize capability/taxonomy for a specific function (e.g., operations, CIO-specific views) - and overlay various metadata (costs, regions, locations, etc. ) with roadmap/timeline to show volatilities/projections. Dynamically generated vantage points.


--------

## Integration

Waltz ships with **no** _out of the box_ integrations.
Integration is typically done via periodic batch jobs which load data into the Waltz database.

Users have integrated with systems such as Collibra, Solidatus, Service Now, Apptio, Sparx Enterprise Architect and more.
We are hoping to offer standardised integrations in the future, with Collibra (Cloud) being the likely first offering.

### Software Development Lifecycle (SDLC)

_Integration with service now to provide controls around dq and enforce regular attestations_


---------


## NFR

### Security

### Standards

### Capacity

Waltz is happily serving several hundred users per day, out of a total user base of tens of thousands.
The Waltz instance has information on over 5K applications and a similar number of change initiatives. There are many more dataflows, surveys, and assessments managed by Waltz.
Several of the taxonomies stretch to thousands of nodes with many more related mappings.

This is all served by a load balanced pair of Tomcat servers with on a shared JVM of just 2GB.
The (SQLServer) database has 8GB of memory.

---------

## Technical Details

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

- Production databases: [MSSQL](https://www.microsoft.com/en-gb/sql-server/), [Postgres](https://www.postgresql.org/)
  - If MSSQL is chosen and you wish to actively work on the Waltz code base a commercial jOOQ licence is required.  jOOQ is free for free databases, but not for commercial databases 
- Test databases: [H2](https://www.h2database.com/html/main.html)

### Deployment options

- Web container, e.g. [Tomcat](https://tomcat.apache.org/)
- Standalone JAR
- [Docker](https://www.docker.com/) image

### Notes



---------

# Current Development Themes 

## 2024

Currently, the main focus for 2024 is to improve support around data flows and lineage.
This will include extending flow classification rules to include 'authoritative consumers', adding a request based model to flow modifications and registration, and improved support for linking EUDAs to flows.

In addition to flows, we expect to look at tools to boost data quality, specifically by allowing organizations to set up correlation rules between taxonomies.
An evaluator would evaluate the rules and raise potential issues where data does not '_look right_'.
For instance a rule could be set up to say that the '_KYC_' capability expects to see '_Person_' related data and be involved in one or more of a set of processes.

Another theme is around the Bank on a Page feature set.
This may involve closer integration with other reporting tools (Tableau, Superset) or we may pursue more custom visualisations.
We also intend to investigate how to integrate more closely with tools such as Archi and other open source diagramming tools.
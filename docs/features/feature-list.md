Original file from Wells Fargo.  They want an open source view of Waltz features and capabilities


Capability   / Feature | Waltz
-- | --
Enterprise Architecture   Management | 
Standards | 
Patterns | 
Authoritative Source | Ability to disposition for a   given capability - what is the authoritative source for the data; can   accommodate for heritage scenarios.
Authoritative Consumers | Ability to identify who should   be the authoritative consumers for a given dataset (bi-directional   relationships for data consumption)
Manage Architectures | 
Models | 
Assessments | 
Portfolio Management | 
Application Rationalization | 
Roadmaps (application,   products) | 
Functional   commissions/de-commissions | Ability to show when   capabilities start/stop within a portfolio
Lifecycle Mappings | 
Data Lineage | Logical flows captured (point to   point with data-type metadata via integration method) with   visualization/mapping abilities.     Significant maturity planned.
Bank / Architecture on a Page | Ability to visualize   capability/taxonomy for a specific function (e.g., operations, CIO-specific   views) - and overlay various metadata   (costs, regions, locations, etc. ) with roadmap/timeline to show   volatilities/projections. Dynamically generated vantage points.
Data Products | Data-based reference view of   underlying datamodels for application portfolio for consumption and usage   purposes.
Cost allocations - Taxonomies | Ability to demarcate the   percentage of costs (shredding) for the purposes of planning/budgeting of a   given function. Application may be performing multiple capabilities and needs   to have appropriate allocations for each functional usage
Application Ratings based on   Capability | Application may have a   legal-hold; supports a given capability and assigning a rating. Providing a   more nuanced/detailed view of what an application is doing specifically   within the capability.
Supply Chain / Contract   Management | Taxonomy of contracts; associate   application with capability/service and with a contract - providing pivot   views of a vendor-relationship what are all the applications linked to it.
Investment Planning | Built-in to the ratings   function/service/capability. Provide a ratings-item which details the   strategic / attribute alignment. Providing driving alignment views for a   given application.
Analytics / Ad-hoc | 
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


## Applications

One of the primary entities in Waltz is the _Application_.
Applications have some basic properties such _name_, _type_, and _description_. 
Additional properties include _lifecycle_ indicators (status, dates) and _organizational ownership_ information.

### Extensions

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

## Taxonomies and Taxonomy Mappings

Organizations can create multiple _Taxonomies_ in Waltz.
Each taxonomy is a hierarchical collection of related elements. 
Examples of these taxonomies are:

- Business Functions
- Capabilities
- Processes
- Regulations
- Agile Tribes
- Records

### Portfolio Management
Organizations can describe and document their application portfolio using _mappings_ to these taxonomies.
Each mapping has an associated _rating_, typically a buy-sell-hold indicator, but the _rating scheme_ can be customised.

The mappings may also be flagged as _primary_ for an application, which allows for more contextual information to be presented when lists of apps are shown elsewhere in Waltz.

### Roadmaps
Decommission and commission information can be associated with each mapping, allowing detailed roadmaps, shwoing how the portfolio will change over time, to be sourced from the captured data.

### Allocations
The mappings may have allocation percentages associated to them, this is commonly used to allow a breakdown of costs per capability for applications (and then aggregated across collections of applications).

### Rating Assessments
Finally, we recently added the ability to add assessments to ratings.
This supports a more nuanced view of the applications, for example we could add product and location assessments to capability ratings indicating what types of financial products and in what location are being handled by a specific application capability.

### Application Rationalisation
Once the application portfolio has been mapped against a consistent set of taxonomies it becomes simple to see high-level opportunities for app consolidation/rationalisation by viewing all applications which are associated to a taxonomy item.
These views are aggregated if higher level items in the taxomomy are selected.

The rating assessments, mentioned above, will provide increased 

--------

## Data Management Features

### Data Types

Waltz supports a hierarchical taxonomy of data types.
Each type may be declared as `concrete` indicating it is sufficiently granular to be used when documenting flows.

### Data Flows

Logical flows (generic) and physical flows (specific descriptions of flows) can be captured and linked to data types.

Each logical flow provides information about a data flow between a source and target system.
These systems are typically applications but may be _Actors_ which are used to represent internal and external entities such as _Exchanges_ and _Risk Officers_.
Taken together, the flows provide a directed graph which shows the raw topology of your organization.

The Waltz UI allows flows to be maintained and has functionality to do more advanced operations such as _merging_ duplicate flows.

Flows and their data types can be attested by either a named set of users or users specifically associated (via a specific role) with the source or target applications.

### Lineage

Waltz has rudimentary support for describing lineage using _Flow Diagrams_, these diagrams are created using a _constrained editor_ which only allows registered flows to be used.

Each diagram is stored as a _Bill-of-Materials_ allowing aggregated information to be calculated, _Aggregate views_ are discussed later.
The diagrams allow for overlaying and filtering, for example you could overlay an indicator showing which of the application in the diagram perform _Trade Capture_.

For more detailed and comprehensive support for lineage we commonly use the data in Waltz with systems such as _Solidatus_.
Alternatively, the information can be exported into a graph database for further analysis using custom tooling.

### Flow Classifications

Rules can be created to capture concepts such as _Authority Statements_.
Each rule defines a source, data type, consumer set, and outcome.
For example: '_BookMgr_ (source) is an _Authoritative Source_ (outcome) of _Book Data_ (data type) for the _Investment Bank_ (consumer set)'.

Defining these rules allows them to be captured and applied to the data flow model to measure 'right sourcing' within an organisation.

### Data Assessments and other extensions

Flows, data types, and related entities can use the standard Waltz enrichment mechanisms.
Particularly useful are assessments, bookmarks, and links to entities such as people (e.g. _Data Owners_) and groups.

--------

## Surveys

--------

## Aggregate views

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
- Taxonimies
- Report grids


#### Bank on a Page

Waltz has the ability to visualize capability/taxonomy for a specific function (e.g., operations, CIO-specific views) - and overlay various metadata (costs, regions, locations, etc. ) with roadmap/timeline to show volatilities/projections. Dynamically generated vantage points.



--------

## Integration

Waltz ships with **no** _out of the box_ integrations.
Integration is typically done via periodic batch jobs which load data into the Waltz database.

Users have integrated with systems such as Collibra, Solidatus, Service Now, Apptio, Sparx Enterprise Architect and more.
We are hoping to offer standardised intgerations in the future, with Collibra (Cloud) being the likely first offering. 

---------

## Technical Specs

### Frontend

Major libraries / frameworks being used:

- Svelte, a lightweight performant framework being used in Waltz to replace the legacy AngularJS code
- D3 for data visualization
- Lodash for data manipulation
- Slickgrid for large tables, being used to replace UI-Grid (AngularJS based grid library)
- Sass, CSS pre-processor
- Webpack, build framework
- Mocha, unit testing

### Server

- Java, currently supporting 8, but shortly moving to JDK 11 or 17 (tbc)
- Spring framework for auto-wiring components
- Spark Java, a lightweight framework for creating REST endpoints
- jOOQ, a java DSL for writing and executing database queries
- Immutables for domain objects

### Database

- Production databases: MSSQL, Postgres
- Test databases: H2

### Deployment options

- Webcontainer, e.g. Tomcat
- Standalone JAR
- Docker image

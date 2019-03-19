# Design for Physical Flow Participants within Waltz

[TOC levels=2-3]:  # "# Contents"

# Contents
- [Status: RELEASED](#status-released)
- [Terminology](#terminology)
- [Motivation](#motivation)
- [Proposal](#proposal)
- [Persistence](#persistence)
    - [Physical Flow Participant table](#physical-flow-participant-table)
    - [Server Information and Database Information tables](#server-information-and-database-information-tables)
    - [Impact / Risks](#impact--risks)
    - [Limitations](#limitations)
- [Example Screens](#example-screens)
- [Possible future enhancements](#possible-future-enhancements)
    - [Resource Information](#resource-information)


## Status: RELEASED

This feature has been implemented and is part of the 1.16 release.


## Terminology

In the following section we will use the following terminology:

- _Logical Flow_: a logical connection between two applications / actors
  representating the flow of data between the source actor / application
  to the _target_ actor / application
- _Physical Flow_: a physical representation of the data transferred in
  a logical flow. The physical flow has attributes to indicate the
  flow's frequency, transport mechanism and a specification of the
  contents transferred.
- _Physical Flow Participant_: the facilitating server, database or resource
  participating on either end of the physical flow.


## Motivation

Waltz represents the transfer of data between sources and recipients in
some detail. This proves useful for architects to gain an understanding
of the information flow between entities. There is a proposal to better
support disaster recovery scenarios for flows by extending existing
information on physical flows with detail of the _participants_ involved in
facilitating them. This will help Ops teams to ensure all relevant
servers and/or databases are recovered to ensure critical application
flows are restored.


## Proposal

1. Represent the _participants_ involved on the source and target ends.
   Examples of _participants_ are _Server_, _Database_, _Service_, _Load
   Balancer_.
2. Ability to view the details of a _Participant_ on a dedicated page, the
   details may include a list of the physical flows and applications
   that involve the aforementioned.
3. Data Quality metrics to highlights physical flows that have no
   relevant disaster recovery annotations.
4. Ability to view all critical flows in aggregate and be able to
   navigate to the relevent participants to assist with an actual disaster
   recovery situation. Export support would be useful in this case.


## Persistence

### Physical Flow Participant table

Participants for physical flows will be captured in the `physical_flow_participant`
table. A row in this table will effectively reference actual servers or
databases that are participating in a physical flow, along with a
designation indicating which side of the flow the participant is acting.

| Column                    | Type      | Mandatory | Description                                                          |
|:--------------------------|:----------|:----------|:---------------------------------------------------------------------|
| `physical_flow_id`        | long      | **y**     | id of the physical flow                                              |
| `side`                    | enum      | **y**     | one of: `SOURCE` or `TARGET` to indicate the side of the flow        |
| `participant_entity_kind` | enum      | **y**     | flow participant entity kind                                         |
| `participant_entity_id`   | long      | **y**     | flow participant entity id                                           |
| `description`             | string    | n         | a description of the nature of the participation of the participant  |
| `last_updated_by`         | string    | **y**     | user id of last editor or creator if new                             |
| `last_updated_at`         | timestamp | **y**     | when this record was created                                         |
| `provenance`              | string    | **y**     | the provenance of the record                                         |

primary key on (`physical_flow_id`, `side`, `participant_entity_kind`, `participant_entity_id`)

### Server Information and Database Information tables

The existing tables `server_information` and `database_information` will
continue to persist servers or databases respectively that are
referenced as participants. The existing tables are denormalised, resulting in
the potential for a server or database to be repeated in several rows if
shared amongst multiple applications.

In order to make servers and databases first class citizens these tables
will require denormalisastion to ensure that each server or database are
represented as a single row in their respective tables.


#### Server Usage table

| Column            | Type      | Mandatory | Description                  |
|:------------------|:----------|:----------|:-----------------------------|
| `server_id`       | long      | **y**     | id of server                 |
| `entity_kind`     | long      | **y**     | the kind of the using entity |
| `entity_id`       | long      | **y**     | the id of the using entity   |
| `last_updated_by` | string    | **y**     | who updated this last        |
| `last_updated_at` | timestamp | **y**     | when this record was created |
| `provenance`      | string    | **y**     | the provenance of the record |


#### Database Usage table

| Column            | Type      | Mandatory | Description                  |
|:------------------|:----------|:----------|:-----------------------------|
| `database_id`     | long      | **y**     | id of database               |
| `entity_kind`     | long      | **y**     | the kind of the using entity |
| `entity_id`       | long      | **y**     | the id of the using entity   |
| `last_updated_by` | string    | **y**     | who updated this last        |
| `last_updated_at` | timestamp | **y**     | when this record was created |
| `provenance`      | string    | **y**     | the provenance of the record |


### Impact / Risks

The refactor of the `server_information` and `database_information`
tables will impact any code that relies on their existing structures. In
core Waltz the following areas would be affected:
- Application / Technology Section
- Technologies Section under Organisation Unit, Person and Measurable
  aggregate pages

Outside of the Waltz core any reporting that is driven off the above
tables will be effected, including entity statistics or management
reports driven by these tables.


### Limitations

The current Waltz data model doesn't deal with some cloud computing
concepts including Serverless and Software as a Service concepts.
Support for these may be incorporated in the future.


## Example Screens

- Participant Information page
  - Overview information on the participant
  - Related applications
  - Physical flows the _participant_ is party to
  - Installed Software Packages on the _participant_
  - Bookmarks
  - Entity Notes, incorporating:
    -   Support contacts
    -   Deployment specific information
- Aggregated view of all resources required for disaster recovery

See [Flow Node mockups](physical_flow_nodes.pdf)


## Possible future enhancements

Providing the ability to better model Serverless computing and Software
as a Service computing in a cloud computing environment.


### Resource Information

The `resource_information` table is potentially generic representation
of compute resource available.

| Column                    | Type      | Mandatory | Description                                               |
|:--------------------------|:----------|:----------|:----------------------------------------------------------|
| `id`                      | seq       | **y**     | PK                                                        |
| `name`                    | string    | **y**     | the name of the resource                                  |
| `external_id`             | string    | **y**     | the external id                                           |
| `environment`             | enum      | **y**     | one of: `DEV`, `UAT`, `PROD` or `DR`?                     |
| `designation`             | enum      | **y**     | one of: `COMPUTE`, `DATABASE`, `LOAD BALANCER`, `STORAGE` |
| `platform`                | enum      | **y**     | one of: `PHYSICAL`, `VIRTUAL`, `CONTAINER`                |
| `description`             | string    | n         | a description of the nature of the participation          |
| `last_updated_by`         | string    | **y**     | user id of last editor or creator if new                  |
| `last_updated_at`         | timestamp | **y**     | when this record was created                              |
| `entity_lifecycle_status` | enum      | **y**     | One of: `ACTIVE`, `PENDING`, `REMOVED`                    |
| `provenance`              | string    | **y**     | the provenance of the record                              |
| `data_center`             | string    | **y**     | reference to another table                                |


Associations to applications can be model using the
`entity_relationship` table.

Client or vendor specific information about resources can be held in the
`entity_notes` table.

#### Association of a resource to other resources

_Open question: There may or may not be a need to associate a resource
with one or more other resources. This may be the case if a resource is
a database that operates on a cluster of physical servers. In such a
scenario the need to associate record describing this database should be
associated with the records for the underlying physical resources. The
same applies if part of the database is stored on a storage solution._

#### Software Package

The `resource_information` table can also be associated with the
`software_package` in order to capture software that is installed or
executing on a resource. The benefits of which include:
- Easily understand in detail the exact usage of a host by viewing the
  software packages it runs
- Quickly identify and report software license costs
- Easily flag resources that need maintenance or upgrades due to
  software version vulnerabilities

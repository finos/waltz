# Requirements for a data feed to support Future Flows

This documents details the data fields required in order to support an 
automated feed for future flows.

Three operations are supported for Future Flows:

1) **Activation** - Is the changing of the **Entity Lifecycle Status** 
of an existing **PENDING** Physical Flow to **ACTIVE**.  The process of 
activating a flow makes it visible in Waltz as all normal flows are.

2) **Modification** - This is the modification of the attributes of a 
physical flow.  Presently this support is restricted to the data-types of 
the Physical Specification which is directly associated with a Physical 
Flow.

3) **Retirement** - The update of the **Entity Lifecycle Status** of a 
Physical Flow to **REMOVED**.

To clarify, all of the above operations require the existence of the 
entity being modified.  In the case of Activation, the entity must 
already exist with the status **PENDING**.  For Modification and 
Retirement it must already exist with the status **ACTIVE**.

Below is the set of fields that would be required to support any 
automated data feed:

| Column                       | Type   | Mandatory | Description                                                                         |
|:-----------------------------|:-------|:----------|:------------------------------------------------------------------------------------|
| Change Set Name              | string | **y**     | name of the change set                                                              |
| Change Set Description       | string | **y**     | description of the change set                                                       |
| Planned Date                 | date   | **y**     | the planned date of the change set                                                  |
| Change Set External Id       | long   | n         | the id of the this change set in whatever source system if originates from          |
| Associated Change Initiative | string | n         | the external id of the change initative that this change set may be associated with |
| Change Unit Name             | string | n         | name of the change unit (if applicable)                                             |
| Change Unit Description      | string | n         | description of the change unit (if applicable)                                      |
| Physical Flow Id             | long   | **y**     | physical flow id                                                                    |
| Action                       | string | **y**     | one of ACTIVATE, MODIFY or RETIRE                                                   |
| Change Unit External Id      | string | n         | id of the change set in whatever source system it originates                        |
| New data type list           | string | n         | a list of the data types that should now apply to a physical specification          |


As detailed above, in order to activate a physical flow in Waltz, the 
expectation is that it already exists in Waltz in `PENDING` state.
As such, in order to facilitate a data feed for future flows, pending flows
may also need to be created via a feed.  The fields of such a feed are detailed
below:

| Column                    | Type   | Mandatory | Description                                                                                                                             |
|:--------------------------|:-------|:----------|:----------------------------------------------------------------------------------------------------------------------------------------|
| Name                      | string | **y**     | A name for the physical flow specification                                                                                              |
| Source                    | string | **y**     | Source of the physical flow (asset code)                                                                                                |
| Target                    | string | **y**     | Target of the physical flow (asset code)                                                                                                |
| Format                    | string | **y**     | Format of the flow, one of: BINARY, DATABASE, FLAT_FILE, JSON, OTHER, UNSTRUCTURED, UNKNOWN, XML                                        |
| Frequency                 | string | **y**     | Frequency of flow, one of: ON_DEMAND, REAL_TIME, INTRA_DAY, DAILY, WEEKLY, MONTHLY, QUARTERLY, BIANNUALLY, YEARLY, UNKNOWN              |
| Transport                 | string | **y**     | Transport of the flow, one of: DATABASE_CONNECTION, EMAIL, FILE_TRANSPORT, FILE_SHARE, MANUAL, MESSAGING, OTHER, RPC, UDP, UNKNOWN, WEB |
| Criticality               | string | **y**     | description of the change unit (if applicable)                                                                                          |
| Basis Offset              | int    | **y**     | a number to indicate the freshness of the data, 1 indicates it is 1 day out of date.                                                    |
| Data Type                 | string | **y**     | a list of data types associated with the physical specification                                                                         |
| External Id               | string | n         | id of the physical in whatever source system it originates                                                                              |
| Description               | string | n         | description of the flow                                                                                                                 |
| Specification Name        | string | n         | name of the physical specification beings transfers, i.e. the payload                                                                   |
| Specification External Id | string | n         | description of the physical specification                                                                                               |
| Status                    | string | **y**     | lifecyle status of the physical flow, one of: ACTIVE, PENDING, REMOVED                                                                  |

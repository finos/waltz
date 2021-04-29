# Custom Environment


## Overview

Waltz now supports the grouping of assets, such as databases and servers, through custom environments.

A new tab has been created within the `Technology Section`, labelled *Environments*

Environment are uniquely named for an owning entity, at the moment there are only applications. The environments are not
shared between applications but servers and databases that are used by applications other than the owning application
can be associated.

Users can create, delete and associate assets to an environment provided they have an involvement with it's owning entity.
The default 'permission group' functionality provides this.

When deleting a custom environment, all associated usages will be deleted from the `custom_environment_usage` table.

Current entity kinds supported in the usage table are:
'SERVER_USAGE' and 'DATABASE'

Custom environment information can also be exported, however this only allowed as .xlsx with split tabs
as the differing attributes between servers and databases prevents a simple csv format.

### Schema

Table: `Custom Environment`

|Column|Description|Example|
|---|---|---|
| id | Primary key for this table | `123` |
| owning_entity_id | Id of the owning entity | `123456` |
| owning_entity_kind | entity kind this environment belongs to  | `APPLICATION` |
| name | Name of this environment |  `UAT` |
| description | Description of environment (optional) |  `Environment for end-user testing` |
| external_id | External identifier (currently auto-generated from owning entity and name) |  `APPLICATION/12345_UAT` |
| group_name | Can be used to group environments together (defaults to 'Default') |  `Default` |

Table: `Custom Environment Usage`

|Column|Description|Example|
|---|---|---|
| id | Primary key for this table | `123` |
| custom_environment_id | id of custom environment environment (fk to `custom_environment`) | `1` |
| entity_id | Id of the associated `server_usage` or `database_information` record | `456` |
| entity_kind | entity kind that has been associated  | `SERVER_USAGE`, `DATABASE` |
| created_at | the time this entity was associated to this environment |  `2021-04-28 17:30:00:00` |
| created_by | the user who associated this entity to this environment |  `john.smith@email.com` |
| provenance | the system that created this record (defaults to 'waltz') |  `waltz` |


### Future Enhancements

Potentially section in server or database view to show which environments it has been linked to.
Support for owning entities other than applications.

---
[Back to ToC](../README.md)
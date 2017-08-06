# Logical Flows

Logical flows represent the existance of a data flow between two entities  (currently any combination of Applications and Actors).  
## Model

![Schema Diagram](logical_flows_schema.png)
[Source](https://app.quickdatabasediagrams.com/#/schema/v2wanPo1hUWpGz0Uk9j2Jg)

### Logical FLow

|Column|Type|Description|
|--|--|--|
|`id`|Long|Identifier|
|`source_entity_kind`|String - `APPLICATION` or `ACTOR`|Kind of the source entity|
|`source_entity_id`|Long|Identifier of ths source entity|
|`target_entity_kind`|String - `APPLICATION` or `ACTOR`|Kind of the target entity|
|`target_entity_id`|Long|Identifier of ths target entity|
|`is_removed`|Boolean|Flag to indicate if this flow has been removed (note this is changing to entity_lifecycle_status soon)|
|`last_updated_at`|Date||
|`last_updated_by`|String||
|`provenance`|String|Where the information about this flow originated|


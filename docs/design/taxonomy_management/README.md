# Design for Taxonomy Management within Waltz

<!-- toc -->

- [Status: DEVELOPMENT](#status-development)
- [Terminology](#terminology)
- [Motivation](#motivation)
- [Commands](#commands)
  * [Commands: Measurable](#commands-measurable)
  * [Commands: Measurable Category](#commands-measurable-category)
  * [Commands: Data Type](#commands-data-type)
- [Persistence](#persistence)
  * [Taxonomy Changelog table](#taxonomy-changelog-table)
- [Command Processing](#command-processing)
  * [Tables potentially impacted by measurable commands](#tables-potentially-impacted-by-measurable-commands)
- [Problems](#problems)
  * [Change ordering](#change-ordering)
    + [Problem](#problem)
    + [Potential mitigation](#potential-mitigation)
- [Security considerations](#security-considerations)
  * [Phase 1 - interim design](#phase-1---interim-design)
  * [Phase 2 - target state design](#phase-2---target-state-design)
- [Example Screens](#example-screens)
- [Notes](#notes)

<!-- tocstop -->

## Status: DEVELOPMENT

This feature is currently being build.  Therefore this document is liable to be modified as new information comes to light.


## Terminology 

In the following section we will use the following terminology:

- _Measurable Category_:  a category applied to a group of measurable items.  Is an instance of a _taxonomy_
- _Measurable_: a specific item within a measurable category.  Is an instance of a _taxonomy item_
- _Data Type_: a specific item in the data type taxonomy


## Motivation

Waltz currently does not allow for the maintenance of measurables or measurable categories 
within the tool.  Currently sites must implement their own mechanisms for measurable management.  Usually 
this is acceptable as the taxonomy is _owned_ by an external system and is automatically 
loaded into Waltz via custom loaders.  However when a taxonomy is wholly owned by Waltz then 
it is desirable that Waltz provide the facility to adequately manage it within the tool.


## Commands
 
### Commands: Measurable 
| Change | Description | Concerns | Priority | Impl. Complexity |
| --- | --- | --- | --- | --- |
| **Rename** | Alter the displayed name of the measurable | Care must be taken to not alter the meaning of the measurable item as all existing references will not reflect the new name | HIGH | LOW |
| **Update description** | Alter the descriptive text of a measurable | Care must be taken to not alter the meaning of the measurable item | HIGH | LOW |
| **Update concrete flag** | desc | concerns | HIGH | LOW |
| **Update externalId** | desc | downstream consumers | HIGH | LOW |
| **Add** | desc | concerns | HIGH | LOW |
| **Move** | desc | concerns | HIGH | MEDIUM |
| **Migrate**| Migrate links/references from one measurable to another | concerns | HIGH | HIGH |
| **Delete** | desc | concerns | HIGH | HIGH |
| **Deprecate** | Marks an item as deprecated and should be no longer considered a valid option.  Used to communicate an intent to remove from a future version of the taxonomy. | Will need ddl update to support. Full gui support may take a while | MEDIUM | MEDIUM / HIGH |


### Commands: Measurable Category 
| Change | Description | Concerns | Priority | Impl. Complexity |
| --- | --- | --- | --- | --- |
| **Rename** | Alter the displayed name of the measurable category | Care must be taken to ensure meaning is not altered. For example if a measurable category consisting of countries was renamed from 'Trading Locations' to 'Processing Locations' then the underlying meaning has been changed and all mappings are potentially invalid | MEDIUM | LOW |
| **Update description** | Alter the descriptive text which describes the measurable category | None | MEDIUM | LOW |
| **Update externalId** | External Ids are typically used when integrating with upstream or downstream systems | Potential unknown impact | LOW | LOW |
| **Delete category** | Remove the category and all items within it [1] | Very destructive. | LOW | MEDIUM |


### Commands: Data Type 
TBD: Similar to Measurable Operations  ?

  
## Persistence
  
### Taxonomy Changelog table
Commands that alter taxonomies will be captured in a new table `taxonomy_changelog`:

| Column | Type | Mandatory | Description | 
| --- | --- | --- | --- |
| `id` | seq | **y** | PK |  - |
| `change_type` | enum | **y** | one of: 'ADD | 
| `change_domain_kind` | enum | **y** | typically either `MEASURABLE_CATEGORY` or `DATA_TYPE` |
| `change_domain_id` | long | **y** | `-1` for data types |
| `kind_a` | enum | **y** | main entity kind this command is operating on |
| `id_a` | long | **y** | main entity id this command is operating on |
| `kind_b` | enum | _n_ | optional secondary entity kind this command is operating on |
| `id_b` | long | _n_ | optional secondary entity id this command is operating on |
| `new_value` | string | _n_ | string value to use in cases of rename etc, may be parsed for boolean value (e.g. concrete flag) |
| `change_status` | enum | **y** | tbc | 
| `created_by` | string | **y** | who created this change user id |
| `created_on` | timestamp | **y** | when this change was created |   
| `executed_date` | string | _n_ | who executed this change |   
| `executed_by` | timestamp | _n_ | when this change was app    lied |   


*Possible extensions* : batching via _changesets_ to indicate a transactional unit of 
work.  


## Command Processing

### Tables potentially impacted by measurable commands 

| Table | Impacting Commands | Comment |
| --- | --- | --- |
| `measurable` | _All_ (except _Migrate_) | - |
| `measurable_rating` | _Merge_, _Delete_, _Migrate_ | - | 
| `entity_hierarchy` | _Move_, _Delete_, _Add_ | - |
| `scenario_axis_item` | _Delete_, _Migrate_ | - |
| `scenario_rating_item` | _Delete_, _Migrate_  | - | 
| `entity_relationship` | _Delete_, _Migrate_ | - |
| `entity_svg_diagram` | _Delete_, _Migrate_ | - | 
| `flow_diagram_entity` | _Delete_, _Migrate_ | - |
| `survey_question_response` | _Delete_, _Migrate_ | - |
| `survey_instance` | _Delete_, _Migrate_ | - |
| `survey_run` | _Delete_, _Migrate_ | - | 
| `bookmark` | _Delete_, _Migrate_ | - | 
| `involvement` | _Delete_, _Migrate_ | - | 


## Problems

### Change ordering

#### Problem
If we disconnect command creation from command execution we may inadvertently remove data.
Consider a taxonomy that looks like:

```
    |- r
      |- c1    (mappings: [])
      |- c2    (mappings: [1, 2, 3])
```

If we merge `c2` into `c1` the result will be the removal of `c2` removed and the mappings `[1, 2, 3]` 
now linked to `c1`.

If we subsequently remove `c1` it will be removed and the mappings will also be deleted.

However if the commands are entered in advance of the processing then the feedback would not show the 
'pending' mappings. This may lead a user to erroneously believing that removing `c1` has no other side-effects.

#### Potential mitigation
If any pending change mentions an entity warn the user.  E.g. 'c1 has a pending merge from c2', the 
user would still be responsible for investigating the potential impact further.


## Security considerations

Security is a broad topic and a comprehensive solution is out of scope of the initial delivery of this
feature.  

Currently the Waltz security model is extremely coarse grained.  Simple roles are associated to users 
via the `user_role` table.  The set of roles is determined by the enum `com.khartec.waltz.model.user.Role`.

### Phase 1 - interim design
We propose to add a new type of Role: `TAXONOMY_EDITOR` which would cover both measurables and data type 
taxonomies. An additional column, `editable`, will be added to `measurable_category` to determine whether a 
category may be modified.  This will help prevent accidental damage to other taxonomies.  

The main drawback to this proposal is the lack of fine-grained control.  Any category that is flagged
as editable may potentially be modified by any person with the `TAXONOMY_EDITOR` role.

### Phase 2 - target state design
Building upon _phase 1_,  instance level _involvement_ associations could be used to indicate
who may manage specific _measurable categories_, _measurable_ subtrees or _data type_ subtrees.
  
This involvement should only be assignable by those with the role: `USER_ADMIN` and would need 
to be referenced by the target nodes.  For example the `measurable_category` table could include 
a column `maintainer_involvement_kind`  which, if present, indicates a required involvement that 
a potential editor _must_ have.


## Example Screens

See [removal example walkthrough](taxonomy_management.pdf).
 

## Notes

[1] - an example of deleting an entire category already exists in
`com.khartec.waltz.jobs.tools.RemoveTaxonomy`

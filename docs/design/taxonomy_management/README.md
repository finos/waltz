# Design for Taxonomy Management within Waltz

## Terminology 

In the following section we will use the following terminology:

- _Measurable Category_:  a category applied to a group of measurable items.  Synonymous with an item in a taxonomy
- _Measurable_: a specific item within a measurable category.  Synonymous with a _taxonomy_


## Motivation

Waltz currently does not allow for the maintenance of measurables or measurable categories 
within the tool.  Currently sites must implement their own mechanisms for measurable management.  Usually 
this is acceptable as the taxonomy is _owned_ by an external system and is automatically 
loaded into Waltz via custom loaders.  However when a taxonomy is wholly owned by Waltz then 
it is desirable that Waltz provide the facility to adequately manage it within the tool.


## Design
 
### Measurable Operations

#### Operations

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



### Measurable Category Operations 

| Change | Description | Concerns | Priority | Impl. Complexity |
| --- | --- | --- | --- | --- |
| **Rename** | Alter the displayed name of the measurable category | Care must be taken to ensure meaning is not altered. For example if a measurable category consisting of countries was renamed from 'Trading Locations' to 'Processing Locations' then the underlying meaning has been changed and all mappings are potentially invalid | MEDIUM | LOW |
| **Update description** | Alter the descriptive text which describes the measurable category | None | MEDIUM | LOW |
| **Update externalId** | External Ids are typically used when integrating with upstream or downstream systems | Potential unknown impact | LOW | LOW |
| **Delete category** | Remove the category and all items within it [1] | Very destructive. | LOW | MEDIUM |


### Data Type Operations 

Similar to Measurable Operations  ?


### Security considerations

**Note this section requires more thought**

Currently the Waltz security model is extremely coarse grained.  Simple roles are associated to users 
via the `user_role` table.  The set of roles is determined by the enum `com.khartec.waltz.model.user.Role`,
in particular the following role is relevant to taxonomy management:

- `CAPABILITY_EDITOR` synonym for measurable editor.

We propose to add a new type of Role: `TAXONOMY_EDITOR` which would cover both measurables and data type taxonomies.
This alone _may_ not be enough to administer a taxonomy.  Instance level _involvement_ associations could be used to indicate
who may manage specific _measurable categories_, _measurable_ subtrees or _data type_ subtrees.  This involvement would
only be assignable by those with the role: `USER_ADMIN` and would need to be differentiated in the `involvement_kind` table.

   
## Command representation:

Commands that alter taxonomies will be captured in a new table `taxonomy_changelog`:


| Column | Type | Mandatory | Description | 
| --- | --- | --- | --- |
| `id` | seq | **y** | PK |  - |
| `change_type` | enum | **y** | one of: 'ADD | 
| `kind_a` | enum | **y** | main entity kind this command is operating on |
| `id_a` | enum | **y** | main entity id this command is operating on |
| `kind_b` | enum | _n_ | optional secondary entity kind this command is operating on |
| `id_b` | enum | _n_ | optional secondary entity id this command is operating on |
| `new_value` | string | _n_ | string value to use in cases of rename etc, may be parsed for boolean value (e.g. concrete flag) |
| `change_status` | enum | _n_ | tbc | 
| `created_by` | string | **y** | who created this change user id |
| `created_on` | timestamp | **y** | when this change was created |   
| `executed_date` | string | _n_ | who executed this change |   
| `executed_by` | timestamp | _n_ | when this change was applied |   


*Possible extensions* : batching via _changesets_ to indicate a transactional unit of 
work.  


## Command processing:

#### Tables potentially impacted by measurable commands 

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



## Notes

[1] - an example of deleting an entire category already exists in
`com.khartec.waltz.jobs.tools.RemoveTaxonomy` 

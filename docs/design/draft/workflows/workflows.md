## workflows


###workflow_definition

| Name        | Type      | Description                          |
|-------------|-----------|--------------------------------------|
| id          | Long      | Sequential identifier                |
| name        | String    | name                                 |
| description | String    | description                          |
| external_id | String    | external id                          |
| created_at  | Timestamp | workflow first created               |
| created_by  | String    | user to create workflow              |
| owning_role | String    | which users can modify this workflow |
| status      | String    | ACTIVE / DRAFT / REMOVED             |

###workflow_state
Describes the different states of a workflow, the state_kind indicates the stage of this state in the workflow

| Name             | Type   | Description                         |
|------------------|--------|-------------------------------------|
| id               | Long   | identifier                          |
| workflow_defn_id | Long   | FK to workflow_definition           |
| name             | String | name                                |
| description      | String | description                         |
| external_id      | String | Description                         |
| state_kind       | String | NOT_STARTED/ IN_PROGRESS /COMPLETED |

###workflow_instance

| Name                   | Type      | Description                         |
|------------------------|-----------|-------------------------------------|
| id                     | Long      | Sequential identifier               |
| workflow_definition_id | Long      | FK to workflow definition           |
| state_id               | Long      | state of this instance              |
| parent_entity_id       | Long      | Entity this workflow is acting on   |
| parent_entity_kind     | String    | Entity this workflow is acting on   |
| external_id            | String    | external id                         |
| created_at             | Timestamp | workflow first created              |
| created_by             | String    | user to create workflow             |
| last_updated_at        | Timestamp | when was this instance last updated |
| last_updated_by        | String    | user to last update this instance   |

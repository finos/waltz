# Feature Summary: Architecture Required Changes (ARC) Survey Integration

## Summary

- Introduces a new "Architecture Required Change" (ARC) concept and integrates ARC selection and responses into the
  Survey subsystem for Change Initiatives with release 1.78.
- **Note:**
    - This is a beta feature. We are actively seeking feedback and contributions from the open-source community to
      enhance it.
    - The ARC functionality has no impact on exsiting surveys unless field type ARC is introduced on the survey
      template.

## Purpose

- Allow survey authors to add a new question field type that lets respondents pick one or more ARCs (Architecture
  Required Changes) related to the survey subject, optionally selecting child ARCs via a hierarchical tree. The ARC
  field is intended for collecting structured ARC selections and associated metadata (dropdown choice, selected ARC
  items, milestone indicators) in a single survey response value.

## User-visible behaviour

- New survey field type: "Architecture Required Change (Beta)" which appears in the survey editor and survey responses.
- In the survey response UI, the ARC field renders a table of candidate parent ARCs (linked to the survey subject). Each
  parent row shows:
    - ARC title (with tooltip and optional external URL link),
    - Milestone RAG and forecast date,
    - A configurable dropdown (e.g., "Y"/"N") that controls whether the respondent should pick items from the ARC tree,
    - A hierarchical tree of child ARCs that the respondent can select when the dropdown indicates inclusion.
- Two modes: VIEW and EDIT. In EDIT, respondents can toggle dropdowns and pick child ARCs; Save persists a JSON payload.
  VIEW shows chosen items and selections.
- The saved response is a JSON string stored in `survey_question_response.json_response` containing an object
  with `responseType: "ARC"` and `responses: [ ... ]` representing each parent ARC row plus selected child items and
  dropdown choices.

## Data model changes

### Database Table: `architecture_required_change`

| Column                    |           Type | Nullable | Description                                     |
|---------------------------|---------------:|:--------:|-------------------------------------------------|
| `id`                      |    BIGINT (PK) |    NO    | Primary key                                     |
| `external_id`             |        VARCHAR |   YES    | External system identifier (indexed)            |
| `title`                   |        VARCHAR |   YES    | ARC title                                       |
| `description`             |           TEXT |   YES    | ARC description                                 |
| `status`                  |        VARCHAR |   YES    | Status string from external system              |
| `milestone_rag`           |        VARCHAR |   YES    | Milestone RAG (Red/Amber/Green)                 |
| `milestone_forecast_date` | DATE/TIMESTAMP |   YES    | Forecast date for milestone                     |
| `external_parent_id`      |        VARCHAR |   YES    | External parent id (indexed)                    |
| `linked_entity_id`        |         BIGINT |   YES    | Waltz entity id this ARC is linked to           |
| `linked_entity_kind`      |   VARCHAR/ENUM |   YES    | Kind of linked entity (e.g., CHANGE_INITIATIVE) |
| `resolution`              |        VARCHAR |   YES    | Resolution or final status                      |
| `provenance`              |        VARCHAR |   YES    | Source provenance                               |
| `entity_lifecycle_status` |        VARCHAR |   YES    | Lifecycle status of the ARC entity              |
| `created_at`              |      TIMESTAMP |    NO    | Created timestamp                               |
| `created_by`              |        VARCHAR |   YES    | Creator username                                |
| `updated_at`              |      TIMESTAMP |   YES    | Last updated timestamp                          |
| `updated_by`              |        VARCHAR |   YES    | Last updater username                           |

Indexes:

- Index on `external_id` (for fast lookup by external id)
- Index on `external_parent_id` (for parent/child tree queries)

### Survey Response: `survey_question_response.json_response`

| Column          |           Type | Nullable | Description                                                                                                                                    |
|-----------------|---------------:|:--------:|------------------------------------------------------------------------------------------------------------------------------------------------|
| `json_response` | VARCHAR / TEXT |   YES    | JSON string payload for complex survey responses (used for `ARC` responses). Contains object with `responseType: "ARC"` and `responses: [...]` |

## APIs: `api/architecture-required-change`

| Method | Path                                                                    | Description                                                                            | Parameters                            | Returns                                                               |
|--------|-------------------------------------------------------------------------|----------------------------------------------------------------------------------------|---------------------------------------|-----------------------------------------------------------------------|
| `GET`  | `/api/architecture-required-change/linked-entity/{kind}/{id}`           | Fetch ARCs directly linked to a specific entity                                        | `kind` (EntityKind), `id` (entity id) | List of `ArchitectureRequiredChange` objects for that entity          |
| `GET`  | `/api/architecture-required-change/linked-entity-hierarchy/{kind}/{id}` | Fetch ARCs linked to an entity and its hierarchy (e.g., change initiative descendants) | `kind`, `id`                          | List of `ArchitectureRequiredChange` objects for the entity hierarchy |
| `GET`  | `/api/architecture-required-change/id/{id}`                             | Fetch single ARC by internal id                                                        | `id` (ARC id)                         | Single `ArchitectureRequiredChange` object                            |

## Configuration & settings

- New settings keys seeded:
    - `arc.external.base.url` — default URL used to link out to an external ARC system.
    - `feature.survey.arc.dropdown-definition` — JSON config controlling dropdown label, options and which option
      indicates inclusion (e.g., {label, options, inclusionOption}).

 
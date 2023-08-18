# Overlay Diagrams

## Overview

Overlay diagrams can be used in a number of ways. Depending on the `diagram_kind` specified, the `layout_data` will be rendered differently. 
This document will describe overlay diagrams of the kind `WALTZ_ENTITY_OVERLAY` which can be constructed using the diagram builder  by system admin and rendered on group pages in the diagram section. 

Entity Overlay Diagrams can are constructed of 'group cells' which may or may not be backed by an entity e.g. a measurable, person or data type. The cell contents can be overlaid with one of

HERE!


## Model

There are 3 main tables for storing legal entity data

- `legal_entity` - describes details of a legal entity
- `legal_entity_relationship_kind` - describes the type of relationship between a legal entity and a target entity kind
- `legal_entity_relationship` - links a legal entity to one or more target entities with a specified relationship kind

Column remarks have been used to describe in further detail the purpose of each field.
![Model](images/legal_entity_model.png)

## Features

- Pages
    - Legal Entity
    - Legal Entity Relationship
    - Legal Entity Relationship Kind
    - Legal Entity Relationship Kind List
- Sections
    - 'Legal Entity Relationships'
        - Displayed on: Legal Entities, Apps, Org Units, App Groups, People, Measurables
    - 'Relationships'
        - Displayed on: Legal Entity Relationship Kind
- Bulk Editor
    - Requires 'Bulk Legal Entity Relationship Editor' role
    - Appears on the 'Relationships' Section for a Legal Entity Relationship Kind
    - Allows users to add, update and remove legal entity relationships and assessments associated to them
    - Detailed guide can be found here:
- Assessments
    - Single and multi-valued assessments can be linked to legal entities, and their relationships
    - Assessments marked as `PRIMARY` for legal entity relationships will be shown as part of the grid visible in the '
      Legal Entity Relationships' and 'Relationships' sections
    - Assessments against legal entity relationships can be edited via the bulk editor

## Bulk Editor

Legal entity relationships can be managed in bulk via the editor screen of the 'legal entity relationship kind' page.

### Input Data

The supported columns are:

| Column Name                                  | Description                                                                                                                                                     | Example               |
|----------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------|
| Entity External Id                           | External Identifier of the entity (usually applications)                                                                                                        | `ABCD`                |
| Legal Entity External Id                     | External Identifier of the legal entity                                                                                                                         | `1234`                |
| Comment                                      | (Optional) Description to be added to the relationship record                                                                                                   | `Some Comment`        |
| Remove Relationship                          | (Optional) Indicates whether to remove this relationship                                                                                                        | `X`                   |
| `{ASSESSMENT_DEFN_EXT_ID} / {RATING_EXT_ID}` | (Optional) Assessment to decorate the relationship. Values of 'Y' or 'X' will indicate alignment, other text is treated as a comment for that assessment rating | `A different comment` |

There are multiple ways to designate assessment values:

1) Use the `{ASSESSMENT_DEFN_EXT_ID} / {RATING_EXT_ID}` format in the header, this gives one column per rating outcome
   and can be used to provide comments per assessment rating
2) Use the `{ASSESSMENT_DEFN_EXT_ID}` format in the header, the values for each of the rows is then interpreted as a `;`
   separated list of rating codes.

The editor is designed to allow copy and paste from an excel doc for easier management.

There are two upload modes to select from:

1) `Add Only` - This will only add or update values for relationships and assessments specified in the file
2) `Replace` - This will replace the detail for any relationships and assessments specified in the file e.g. If the
   target entity and legal entity details are specified but a value in an assessment column has been removed, this will
   delete that assessment rating.

Note: `Replace` will not remove a relationship, only the assessment detail, to completely remove a legal entity
relationship place an 'X' in the 'Remove Relationship' column.

Select 'Search' to continue.

### Upload Preview

After searching a resolve summary will be shown for the input data.
This lists out the operation for the relationship - `ADD`, `UPDATE` or `REMOVE` - and highlights any errors with the
input data.
You can hover over the warning symbol for more detail on the error.

Example errors:

- An unrecognised Entity External Id
- An unrecognised Legal Entity External Id
- A command to remove a relationship that does not exist
- An assessment column header that cannot be identified
- An assessment rating value that cannot be identified
- A breach of a single-valued assessment definition cardinality by defining multiple ratings

You can either correct the errors in the input by selecting 'Edit Input' or select the 'enable save' button in order to
proceed; any relationships with errors will be ignored.

![Upload Preview](images/upload_preview.png)

Select 'Save Relationships' to continue.

### Upload Summary

After saving the upload preview a summary is shown.

![Upload Summary](images/upload_summary_result.png)

Select 'Done' to close the editor.

### Relationships Section

The updates to relationships and assessments should immediately be visible in the table view.

![Assessments Table](images/le_rel_assessment_table.png)

### Example Input

| Entity External Id | Legal Entity External Id | Comment              | Remove Relationship   |
|--------------------|--------------------------|----------------------|-----------------------|
| `ENTITY_EXT_ID`    | `LEGAL_ENTITY_EXT_ID`    | `COMMENT` (optional) | `X` or `Y` (optional) |

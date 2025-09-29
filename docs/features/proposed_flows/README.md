# Waltz Maker-Checker Workflow

## Overview

The Waltz Maker-Checker workflow is designed to bring robust governance and data quality (DQ) control to the management of data flows. This workflow ensures that all critical changes to logical and physical flows pass through a two-step approval process, supporting compliance and audit requirements while increasing metadata coverage as required by Data Owners (DOs) and DQ standards.

---

## Key Scenarios & Challenges

1. **Dynamic Logical Flow Definitions**  
   Logical flow definitions evolve as physical flows (both manual and auto-generated) are created or modified.

2. **High Volume of Logical Flows**  
   There are often hundreds of logical flows with no physical flows attached, making governance challenging.

3. **Tight Data Model Coupling**  
   The data model's reliance on logical flows means changes must be carefully managed to avoid system-wide impacts.

4. **Controlled Logical Flow Creation**  
   Logical flows must exist for system integrity, but user-initiated creation should be restricted.

---

## Workflow Actions

| Action                     | Maker            | Checker (Approver)            | Notes                                      |
|----------------------------|------------------|-------------------------------|--------------------------------------------|
| Create Manual Logical Flow | Propose          | Source Approver, Target Approver | Drafted, then routed for approvals         |
| Approve Logical Flow       | -                | Source/Target Approver        | Moves to Approved state                    |
| Create Physical Flow       | (After Approval) | -                             | Allowed only for approved logical flows    |

---

## High-Level Design

### Data Migration

- **Goal:** For logical flows without physical flows, auto-create a physical flow definition via a one-time data loader.
    ```sql
    SELECT lf.id AS "only-logical-flow", pf.id
    FROM logical_flow lf
    LEFT JOIN physical_flow pf ON lf.id = pf.logical_flow_id
    WHERE pf.id IS NULL;
    ```

### Flow Creation
The workflow supports two primary user journeys for creating flows, both governed by the Maker-Checker process.

1.  **Propose a New Logical and Physical Flow:**
    -   **Maker Action:** A user (the "Maker") initiates the creation of a new flow by providing the `source`, `target`, and at least one `data type`.
    -   **System Action:**
        -   A new entry is created in the `proposed_flow` table.
        -   The `flow_def` JSON field is populated with all necessary details for both the logical and physical flow (e.g., transport mechanism, frequency, data types).
        -   A workflow is initiated for this `proposed_flow` entity, starting in the `PENDING_APPROVALS` state.
    -   **Approval:** The proposal is routed to the designated approvers for the source and target applications.
    -   **Finalization:** Upon receiving `FULLY_APPROVED` status, the system creates the `logical_flow` and the corresponding `physical_flow` records from the `flow_def` data.

2.  **Attach a New Physical Flow to an Existing Logical Flow:**
    -   **Maker Action:** The Maker selects an existing, approved `logical_flow` and proposes a new `physical_flow` to be attached to it.
    -   **System Action:**
        -   A new entry is created in the `proposed_flow` table, referencing the existing `logical_flow_id`.
        -   The `flow_def` will contain only the details for the new physical flow.
        -   The proposal enters the same approval workflow, starting at `PENDING_APPROVALS`.
    -   **Approval:** The proposal is routed for approvals.
    -   **Finalization:** Once `FULLY_APPROVED`, the system creates the new `physical_flow` and links it to the existing `logical_flow`.

### Proposed Flow Table

A new `proposed_flow` table stores draft flow definitions:

| Field               | Type                 | Description                |
|---------------------|----------------------|----------------------------|
| id                  | `long`               | Unique identifier          |
| type                | `string`             | The type of proposal (e.g., `CREATE`, `UPDATE`, `REMOVE`) |
| description         | `string`             | A description of the proposed change |
| source_entity_id    | `long`               | Source application ID      |
| source_entity_kind  | `string`             | Source entity type (e.g., `APPLICATION`) |
| target_entity_id    | `long`               | Target application ID      |
| target_entity_kind  | `string`             | Target entity type (e.g., `APPLICATION`) |
| created_by          | `string`             | User ID of the creator     |
| created_at          | `timestamp`          | Timestamp of creation      |
| flow_def            | `json`               | JSON object with all flow details |

#### `flow_def` JSON Structure

The `flow_def` field contains a JSON object that captures all the necessary information to create or update logical and physical flows. Below is a breakdown of its key attributes:

| Attribute | Type | Description |
|---|---|---|
| `source` | `EntityReference` | The source application or actor of the flow. |
| `target` | `EntityReference` | The target application or actor of the flow. |
| `reason` | `Reason` | The justification for the flow proposal. |
| `logicalFlowId` | `long` (nullable) | The ID of an existing logical flow if attaching a new physical flow. `null` for a new logical flow proposal. |
| `physicalFlowId` | `long` (nullable) | The ID of an existing physical flow if modifying it. `null` for a new physical flow proposal. |
| `specification` | `PhysicalSpecification` | The specification for the physical flow, defining its format and owning entity. Can be a new or existing specification. |
| `flowAttributes` | `FlowAttributes` | The characteristics of the physical flow, including transport, frequency, and criticality. |
| `dataTypeIds` | `array[long]` | A list of data type IDs associated with the flow. |
| `proposalType` | `string` | The type of proposal, e.g., `CREATE`, `UPDATE`, `REMOVE`. |


### Workflow State Tracking

Leverages existing workflow tables:

- **entity_workflow_definition:** Defines the workflow for proposed flows.
- **entity_workflow_state:** Tracks workflow state of each proposed flow.
- **entity_workflow_transition:** Records state transitions and reasons.

#### Example State Transitions

| Step         | State Before       | Action                 | State After        | By               |
|--------------|-------------------|------------------------|--------------------|------------------|
| 1. Proposal  | -                 | Maker Proposes         | PENDING_APPROVALS  | Maker            |
| 2. Approve 1 | PENDING_APPROVALS | Source Approver Approves| SOURCE_APPROVED    | Source Approver  |
| 3. Approve 2 | SOURCE_APPROVED   | Target Approver Approves| FULLY_APPROVED     | Target Approver  |

**Sample Table Data:**

#### `entity_workflow_state`
  | workflow_id | entity_id | entity_kind   | state             | description                                 |
  |-------------|-----------|--------------|-------------------|---------------------------------------------|
  | 2           | 678       | proposed_flow| PENDING_APPROVALS | Maker has proposed a Create Flow            |
  | 2           | 678       | proposed_flow| SOURCE_APPROVED   | Source Approver approved the Create Flow    |
  | 2           | 678       | proposed_flow| FULLY_APPROVED    | Fully approved by all required approvers    |

#### `entity_workflow_transition`
  | workflow_id | entity_id | entity_kind   | from_state         | to_state         | reason                           |
  |-------------|-----------|--------------|--------------------|------------------|----------------------------------|
  | 2           | 678       | proposed_flow| PROPOSED_CREATE    | PENDING_APPROVALS| Maker has proposed a Create Flow |
  | 2           | 678       | proposed_flow| PENDING_APPROVALS  | SOURCE_APPROVED  | Approved by Source Approver      |
  | 2           | 678       | proposed_flow| SOURCE_APPROVED    | FULLY_APPROVED   | Approved by Target Approver      |

---

## State Machine & Transitions

All state transitions are handled by a state machine, validating actions and determining allowed next states.

**Main Transitions:**

![Possible Transitions](images\transitions.png)

- `PROPOSED_CREATE` → `PENDING_APPROVALS`
- `PENDING_APPROVALS` → `SOURCE_APPROVED`
- `PENDING_APPROVALS` → `TARGET_APPROVED`
- `PENDING_APPROVALS` → `SOURCE_REJECTED`
- `PENDING_APPROVALS` → `TARGET_REJECTED`
- `SOURCE_APPROVED` → `FULLY_APPROVED`
- `TARGET_APPROVED` → `FULLY_APPROVED`
- ...and more as needed.

A new state metadata table is introduced for flexible transition management.

---

## Benefits

- **Comprehensive Governance:** All changes are audited and approved.
- **Improved Data Quality:** Ensures flows are validated before activation.
- **Scalability:** Designed for high volumes of flows and flexible for future enhancements.
- **Extensible:** New states, transitions, and roles can be added as requirements grow.

---

> **Contributions and feedback are welcome!** 

---
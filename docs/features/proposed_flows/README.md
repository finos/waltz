# Waltz Proposed Flow Workflow

## Overview

The Waltz Proposed Flow workflow is designed to bring robust governance and data quality (DQ) control to the management
of data flows. This workflow ensures that all critical changes to logical and physical flows pass through a two-step
approval process, supporting compliance and audit requirements while increasing metadata coverage as required by Data
Owners (DOs) and DQ standards.

---

## Challenges With Existing Logical/Physical Flow Creation

1. **No Approval Mechanism for Flow Creation**  
   Currently the logical and/or physical flows can be created without seeking any approval between the source and target
   system owners.

2. **Logical Flows With No Physical Flows Attached**  
   There are often hundreds of logical flows with no physical flows attached, making governance challenging.

3. **Tight Data Model Coupling**  
   The data model's reliance on logical flows means changes must be carefully managed to avoid system-wide impacts.

4. **Controlled Logical Flow Creation**  
   Logical flows must exist for system integrity, but user-initiated creation should be restricted.

---

## Workflow Actions

| Action                     | Maker            | Checker (Approver)               | Notes                                   |
|----------------------------|------------------|----------------------------------|-----------------------------------------|
| Create Manual Logical Flow | Propose          | Source Approver, Target Approver | Drafted, then routed for approvals      |
| Approve Logical Flow       | -                | Source/Target Approver           | Moves to Approved state                 |
| Create Physical Flow       | (After Approval) | -                                | Allowed only for approved logical flows |

---

## Proposed Flow Workflow States & Transitions

All state transitions are handled by a state machine, validating actions and determining allowed next states.

![Possible Transitions](images\proposed_flow_transitions.png)

---

## Benefits

- **Comprehensive Governance:** All changes are audited and approved.
- **Improved Data Quality:** Ensures flows are validated before activation.
- **Scalability:** Designed for high volumes of flows and flexible for future enhancements.
- **Extensible:** New states, transitions, and roles can be added as requirements grow.

---

> **Contributions and feedback are welcome!**

---
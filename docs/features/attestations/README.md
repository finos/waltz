# Attestations

## Overview

Attestations allow for users to confirm the validity of
various aspects of entities they are involved with.  Currently,
attestations are available for applications where users can
confirm the validity of logical and physical data flows, or measurables.
Attestations can be completed in two places: 
- They can be issued to users via an attestation run. These attestations appear in your notifications on the profile bar or on the homepage.
- They can also be attested via the application view as an ad-hoc attestation.
  Currently only available for flows.


## Attestation Runs
The set of target entities
for the attestation run is derived from a Waltz selection mechanism
(e.g. apps under a specific organisational unit, application groups, apps performing
a specific function etc.). The attestations are then issued based upon a given set of roles describing
their involvement with the selected entities. 

![Screenshot](images/attestation_screenshot.png)

## Ad-Hoc Attestations
Performed in the attestation section, the user must have a direct involvement with the application 
to attest.

Attestations for logical flows can only be performed if:
- There are flows documented for the application
- There are no flows with unknown datatypes
- There are no flows with deprecated datatypes

For each of these rules there is the ability to exempt an application from the rule; 
these are controlled by application groups. The `external_id` field on the application group you wish to use 
can be set to one of the following:
- 'LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_FLOW_COUNT_CHECK'
  - membership to this group allows logical flows to be attested even if there are no flows registered for the application.
- 'LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_UNKNOWN_DATA_TYPE_CHECK'
  - membership to this group allows flows to be attested even if there is an unknown datatype associated to a flow.
- 'LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_DEPRECATED_DATA_TYPE_CHECK'
    - membership to this group allows flows to be attested even if there is an deprecated datatype associated to a flow.

Being exempt in one group, does not make you exempt from the other checks. 


## Other info
When you attest for an entity, any other pending attestations that are assigned to you for that entity
and subject kind (e.g. logical flows) will also be closed off. 

There is a job in the `ScheduledJobService` that will issue any instances for pending runs. These are attestation runs with
a `status` field of 'PENDING'. Currently these cannot be configured through the gui. 

A video walkthrough for attestations can be viewed on [Youtube](https://www.youtube.com/watch?v=dZVWKXxzZWQ).


## Model

![Schema Diagram](images/attestation_schema.png)


---
[Back to ToC](../README.md)
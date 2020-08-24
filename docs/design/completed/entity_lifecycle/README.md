# Entity Lifecycle

Lifecycle management of entities in Waltz in managed using two fields:
- `is_removed`
- `entity_lifecycle_status`: `ACTIVE | PENDING | REMOVED`

`is_removed` is intended to track the records lifecycle status.  This fields 
affects the visibility of the record to Waltz.  If the field's value is **true**
this indicates that Waltz doesn't have visibility and effectively implies deletion
of the record in question (although soft, to allow later recovery if required).

`entity_lifecycle_status` represents the lifecycle of the entity.  All values of
this field are visible to Waltz.  
- `ACTIVE` indicates that the entity is live and in use
- `PENDING` indicates that the entity is not live but pending commissioning,
i.e. an application that in due to go live.
- `REMOVED` indicates a decommissioned entity.  Whilst the `is_removed` flag 
is **true** this entity is still visible to Waltz and will be highlighted as 
decomissioned.

NOTE: when an entity is deleted, `is_removed` field with be toggled to **true**
thereafter the entity will not be included in any Waltz selectors.  
The `entity_lifecycle_status` should remain unchanged to ensure a restore operation
would return the entity to it's last lifecycle status.

#### Entity Selectors
All entity selectors in Waltz should explicitly ensure that a restriction of
_is_removed = false_ is  applied to ensure deleted entities are **NOT** returned
in results.
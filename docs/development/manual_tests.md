# UI Testing checklist for Waltz


## Applications
- [ ] Add / remove tag
- [ ] Add / remove alias
  - [ ] verify searchable
- [ ] edit app (desc, rating, phase etc)

## Bookmarks
- [ ] add / remove / edit
- [ ] search / filter

## Involvement
- [ ] add / remove / edit
  - [ ] ensure `user_selectable` flag is respected 
- [ ] search / filter

## Attestations
- [ ] attest measurables
- [ ] attest logical flows
- [ ] attest physical flows
- [ ] update permissions to add/remove attestation capability

## Measurables
- [ ] add/remove/edit ratings
  - [ ] add/update comments 
  - [ ] add decomm/revoke decomm
  - [ ] add/remove replacement apps
  - [ ] add/remove allocations
  - [ ] check little icon overlays are indicating decomm/replacements/allocations
- [ ] roll-ups
- [ ] inferred relationships

## Technology
- [ ] displays app level data
- [ ] can search for servers
- [ ] rolls-up 
- [ ] exportable via aggregate pages

## Flows
UI tests for Logical, Physical flows and Lineage 

- [ ] Create logical flow
- [ ] Create physical flow
- [ ] Create lineage
- [ ] Edit lineage
- [ ] Delete lineage
- [ ] Add newly created physical to existing lineage
- [ ] Delete logical flow which has physical flows already, should be prevented
- [ ] Delete physical flow which is already used in lineage, should be prevented
- [ ] Create a flow classification rule, check the logical flow view is updated with new rating/color


## Admin
- [ ] grant/remove user roles
- [ ] add custom role
- [ ] create/update/remove actor
- 


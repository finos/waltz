# Entity Relationships

## Overview

Waltz allows ad hoc relationships between two entities to be represented in the `entity_relationship` table.  In order
to represent a relationship.



This table encapsulates two entity references and a relationship kind.  A relationship kind can be directional,
in which case it is matters which entity is on side A or B of the entity relationship table.

The available relationship kinds are as follows:

- `HAS`
- `PARTICIPATES_IN`
- `LOOSELY_RELATES_TO`
- `RELATES_TO`
- `SUPPORTS`
- `DEPRECATES`

Has, Participates In and Supports are directional relationships and so when representated in the Waltz Entity 
Relationship table would read:

__Entity A__ - _HAS_ - __Entity B__  
__Entity A__ - _PARTICIPATES_IN_ - __Entity B__  
__Entity A__ - _SUPPORTS_ - __Entity B__  


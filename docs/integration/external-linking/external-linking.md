# External Linking 

Waltz supports both linking to outgoing external resources and also attempts to resolve 
incoming links.

## Outgoing

Outgoing links are typically recorded in the _Bookmark_ section on entity pages.  

## Incoming 
Incoming links are supported via urls which have the Waltz system id's in them or in special cases 
Waltz supports incoming links via an entities _external id_ (or _asset code_).


|Entity Type| System Url | Alternative Url | 
| --- | --- | --- | 
| Application | `<waltz>/application/{id}` |  `<waltz>/application/external-id/{extId}` |
| Application | `<waltz>/application/{id}` |  `<waltz>/application/asset-code/{extId}` |
| Change Initiative | `<waltz>/change-initiative/{id}` | `<waltz>/change-initiative/external-id/{extId}` |
| Data Type | `<waltz>/data-types/{id}` | `<waltz>/data-types/code/{extId}` |
| Data Type | `<waltz>/data-types/{id}` | `<waltz>/data-types/external-id/{extId}` |
| Logical Data Element | `<waltz>/logical-data-element/{id}` | `<waltz>/logical-data-element/external-id/{extId}` |
| Measurable | `<waltz>/measurable/{id}` | `<waltz>/measurable/external-id/{categoryExtId}/{itemExtId}` |
| Server | `<waltz>/server/{id}` | `<waltz>/server/external-id/{extId}` |
| Server | `<waltz>/server/{id}` | `<waltz>/server/hostname/{hostname}` |
 

_Note:_ Some entities support dual addressing mechanisms.  This is because technically 
 the more specific term is correct, however for consistency `external_id` is also 
supported.
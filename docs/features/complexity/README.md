# Complexity


## Overview

Waltz can calculate a basic complexity score for each application.  The current 
implementation uses a formula like:

```
maxConn = maximumNumberOfLogicalFlowsForAnyApp
appConn = totalNuberOfLogicalFlowsForThisApp

maxServers = maximumNumberOfServersForAnyApp
appServers = totalNuberOfServersForThisApp

maxViewpoints = maximumNumberOfViewpointsUsedByAnyApp
appViewpoints = totalNumberOfViewpointsUsedByThisApp

connectionComplexity = log(appConn) / log(maxConn)
serverComplexity = log(appConn) / log(maxConn)
viewpointComplexity = appViewpoints / maxViewpoints
            
totalComplexity = serverComplexity + connectionComplexity + viewpointComplexity
            
```

Note that server and connection complexities are scaled so that the marginal 'cost' of 
adding new servers and connections diminishes.  However viewpoint complexity does not.




### Schema

Table: `Complexity Score`

|Column|Description|Example|
|---|---|---|
| entity_kind | Kind of entity being scored | `APPLICATION` |
| entity_id | Id of entity being score | `123456` |
| complexity_kind | type of complexity being measured | `MEASURABLE` |
| score | (decimal 10,3) amount |  `0.7` |


Valid complexity kinds:

- `CONNECTION`
- `MEASURABLE` (viewpoint)
- `SERVER`
- `TOTAL`


### Future Enhancements

Plan is to allow for custom complexity kinds to be introduced an provide a simple 
way for developers to plug in their own calculators.


---
[Back to ToC](../README.md)
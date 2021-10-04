# Standard Loader  

----

## Data loaders are similar to ETL jobs
### Follow a common pattern:

```java
Set externalApps = fetchAppsFromExternalSource();
Set existingApps = fetchAppsFromWaltz();

Set toAdd = minus(externalApps, existingApps);
Set toRemove = minus(existingApp, externalApps);
Set toUpdate = instersection(externalAps, existingApps, comparatorFn);

batchInsert(toAdd);
batchUpdate(toRemove); // soft deletes
batchUpdate(toUpdate); // update attrs

writeChangeLogs(toAdd, toRemove, toUpdate); // user visible log
```

A good example is: [HigherEducationTaxonomyImport.java](https://github.com/finos/waltz/blob/master/waltz-jobs/src/main/java/com/khartec/waltz/jobs/example/HigherEducationTaxonomyImport.java).



----

_[prev](50_setup.md)_ |
_[next](70_use_cases.md)_


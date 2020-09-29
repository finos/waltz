# Working with the Waltz Schema

## Naming standards

- Singular naming (e.g. `person` not `people`, `application` not `applications`)
- Define _everything_ in _Liquibase_
- Use the types defined in `db.properties.xml` for defining ids, comments, enums etc.  
  - This allows us to be database independent, consistent and also make a bulk change if needed
- Names should not exceed 30 chars
  - (in case we need to support Oracle)
- Index/Constraint(etc) names should be unique in the database
  - Restriction imposed by Postgres


## Table/Column Deletion
 
- An issue should be created and flagged with both the `DDL Change` and `Removal` tags
- Deletion should only happen 2 point releases after the feature code has been removed
  - e.g. TourGuides were removed in 1.4 but the tables were not scheduled for deletion until 1.6
- Deletion should be performed via _Liquibase_
- The associated issues should be bubbled up to the relevant release notes


## Rollback

Liquibase supports DDL rollback.  As of Waltz 1.25 you may rollback the schema by issuing 
the `rollback <tagname>` command.  e.g.:

```shell script
$ ./liquibase-pg.sh rollback v1.25
```



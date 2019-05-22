# Taking and restoring database backups

Status: DRAFT


## Creating Backups

### Maria 
Maria data can be exported using the `mysqldump` utility.

Example:
````
mysqldump -u {user} {database} > dump.sql
````

Note: on linux you may need to modify `/usr/local/etc/my.cnf`. 

### Postgres
Postgres data can be exported using the `pg_dump` utility.

Example:
```
pg_dump -d {database} -U {user} > dump.sql
```


## Restoring Backups

### Maria 
Example
````
 mysql -u {user} {database} < dump.sql
````

### Postgres
Example:
```
psql -U {user} {database} < dump.sql
```

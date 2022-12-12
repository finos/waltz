# Taking and restoring database backups

Status: DRAFT


## Creating Backups

### Postgres

Postgres data can be exported using the [`pg_dump`](https://www.postgresql.org/docs/current/app-pgdump.html) utility.

Usage:
```
pg_dump -d {database} -U {user} --format=tar  > dump.sql
```

Example (taking a backup of waltz database, db1):
```
pg_dump -U waltz --port 5632 --format=tar waltzdb1 > d:\dev\data\postgres-dump-1.46.sql
```


You will probably want to `gzip` it down afterwards.


## Restoring Backups

### Postgres
Usage:
```
psql -U {user} {database} < dump.sql
```

Example (restoring into a new blank database, db2): 
```
pg_restore.exe -d waltzdb2 -U waltz --port 5632 <  c:\temp\postgres-dump-1.46.sql
```

